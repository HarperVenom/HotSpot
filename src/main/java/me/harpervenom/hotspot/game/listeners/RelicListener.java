package me.harpervenom.hotspot.game.listeners;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.game.profile.GameProfile;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.game.listeners.ExplosionListener.isInteractable;
import static me.harpervenom.hotspot.game.vault.loot.CustomItems.*;
import static me.harpervenom.hotspot.utils.Utils.playSound;
import static me.harpervenom.hotspot.utils.Utils.text;

public class RelicListener implements Listener {

    private final GameManager gameManager;

    private final Map<World, BukkitTask> weatherTasks = new HashMap<>();

    public RelicListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onThunderRelic(PlayerInteractEvent e) {
        // Check if the player right-clicked with the Thunder Scroll in hand
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null) return;

        if (item.isSimilar(thunderRelic)) {
            if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
            if (e.getClickedBlock() != null && isInteractable(e.getClickedBlock().getType())) return;

            e.setCancelled(true);

            // Get the world
            World world = player.getWorld();

            // Cancel the previous task if there's one associated with the world
            if (weatherTasks.containsKey(world)) {
                BukkitTask previousTask = weatherTasks.get(world);
                previousTask.cancel(); // Cancel the existing thunderstorm task
            }

            // Set the world weather to thunderstorm
            world.setWeatherDuration(thunderDuration * 20);
            world.setStorm(true); // Enable thunderstorm weather
            world.setThundering(true); // Enable thunder

            // Send a message to the player
            player.sendMessage(text("Вы призвали шторм...", NamedTextColor.BLUE));
            Game game = gameManager.getGame(world);
            if (game != null) playSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 0.7f, game.getPlayers());

            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Revert weather back to normal
                world.setStorm(false);
                world.setThundering(false);

            }, thunderDuration * 20);

            // Store the task in the map
            weatherTasks.put(world, task);

            // Optionally, consume the Thunder Scroll
            item.setAmount(item.getAmount() - 1);
        }
    }

    @EventHandler
    public void onPillarRelic(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null) return;

        if (item.isSimilar(pillarRelic)) {
            if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
            if (e.getClickedBlock() != null && isInteractable(e.getClickedBlock().getType())) return;
            e.setCancelled(true);

            World world = player.getWorld();
            Game game = gameManager.getGame(world);
            if (game == null) return;
            GameMap map = game.getMap();

            world.playSound(player.getEyeLocation(), Sound.UI_STONECUTTER_TAKE_RESULT, 2, 0.7f);
            player.setFallDistance(0);

            // Determine the entity to move: player or mount
            Entity target = player.getVehicle() != null ? player.getVehicle() : player;

            int liftTicks = 10;       // ticks spent lifting
            int extraTicks = 10;      // ~0.5s after lift for block placing
            double push = 0.8;        // upward velocity per tick

            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (!player.isOnline() || target.isDead()) {
                        cancel();
                        return;
                    }

                    if (ticks < liftTicks) {
                        // lift
                        target.setVelocity(target.getVelocity().setY(push));
                    } else if (ticks > liftTicks + extraTicks) {
                        // done after extra placing time
                        cancel();
                        return;
                    }

                    // place block under target (during lift AND extra phase)
                    Location below = target.getLocation().clone().subtract(0, 1, 0).getBlock().getLocation();
                    Block b = below.getBlock();
                    if (map.canPlace(b) && b.getType().isAir()) {
                        b.setType(Material.BROWN_CONCRETE_POWDER);

                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            world.spawnParticle(
                                    Particle.BLOCK,
                                    b.getLocation().clone().add(0.3, 0.3, 0.3),
                                    20, 0.5, 0.5, 0.5, 0.1,
                                    Bukkit.createBlockData(Material.BROWN_CONCRETE_POWDER)
                            );
                        }, 1);

                        world.playSound(b.getLocation(), Sound.BLOCK_GRAVEL_PLACE, 1, 1);
                        map.addBlock(b);
                    }

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            item.setAmount(item.getAmount() - 1);
        }
    }

    @EventHandler
    public void onReflectionRelic(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();
        Entity damagerEntity = e.getDamager();

        Game game = gameManager.getGame(entity.getWorld());
        if (game == null) return;
        if (!(damagerEntity instanceof Player damager)) return;
        GameProfile damagerProfile = game.getPlayerManager().getProfile(damager);
        if (damagerProfile == null) return;

        if (e.isCancelled()) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (!(entity instanceof Player victim)) return;

        ItemStack relicUsed = null;
        EquipmentSlot slotUsed = null;

        // Check main hand
        if (victim.getInventory().getItemInMainHand().isSimilar(reflectionRelic)) {
            relicUsed = victim.getInventory().getItemInMainHand();
            slotUsed = EquipmentSlot.HAND;
        }
        // Check off hand
        else if (victim.getInventory().getItemInOffHand().isSimilar(reflectionRelic)) {
            relicUsed = victim.getInventory().getItemInOffHand();
            slotUsed = EquipmentSlot.OFF_HAND;
        }

        if (relicUsed == null) return;

        Entity target = damagerEntity.getVehicle() != null ? damagerEntity.getVehicle() : damagerEntity;

        // Apply knockback
        double multiplier = Math.max(2, e.getFinalDamage() * 0.3);
        Vector direction = damagerEntity.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize().multiply(multiplier);
        direction.setY(Math.max(1, direction.getY()));

        if (!damagerProfile.isProtected()) {
            // Launch the player in the opposite direction
            for (int i = 0; i < 3; i++) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    target.setVelocity(direction);
                }, i);
            }
        }

        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1, 0.8f);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 0.8f);

        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromBGR(129, 0, 161), 1);
        victim.getWorld().spawnParticle(Particle.DUST, victim.getLocation().clone().add(0, 1, 0)
                , 50, 0.5, 0.5, 0.5, 0, dustOptions, true);

        // Decrease the amount or remove the item
        if (relicUsed.getAmount() > 1) {
            relicUsed.setAmount(relicUsed.getAmount() - 1);
        } else {
            if (slotUsed == EquipmentSlot.HAND) {
                victim.getInventory().setItemInMainHand(null);
            } else {
                victim.getInventory().setItemInOffHand(null);
            }
        }
        game.getDamageManager().assignLastDamager(damagerEntity, victim);
    }
}
