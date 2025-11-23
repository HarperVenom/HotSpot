package me.harpervenom.hotspot.game.listeners;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Random;

public class SpawnEggListener implements Listener {

    private final GameManager gameManager;

    public SpawnEggListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    private final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerUseEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!(item.getType() == Material.HORSE_SPAWN_EGG ||
                item.getType() == Material.CAMEL_SPAWN_EGG ||
                item.getType().toString().endsWith("_SPAWN_EGG"))) {
            return; // not a spawn egg
        }

        // Prevent normal baby spawning
        event.setCancelled(true);

        Location loc = event.getRightClicked().getLocation().add(1, 0, 0);

        if (item.getType() == Material.HORSE_SPAWN_EGG) {
            Horse horse = (Horse) loc.getWorld().spawnEntity(loc, EntityType.HORSE);
            horse.setTamed(true);
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));

            Material[] armors = {
                    Material.DIAMOND_HORSE_ARMOR,
                    Material.GOLDEN_HORSE_ARMOR,
                    Material.IRON_HORSE_ARMOR,
                    Material.LEATHER_HORSE_ARMOR
            };
            horse.getInventory().setArmor(new ItemStack(armors[random.nextInt(armors.length)]));
        } else if (item.getType() == Material.CAMEL_SPAWN_EGG) {
            Camel camel = (Camel) loc.getWorld().spawnEntity(loc, EntityType.CAMEL);
            camel.setTamed(true);
            camel.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerUseBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!(item.getType() == Material.HORSE_SPAWN_EGG || item.getType() == Material.CAMEL_SPAWN_EGG)) return;

        // Cancel normal spawn
        event.setCancelled(true);

        if (event.getClickedBlock() == null) return;
        Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();

        if (item.getType() == Material.HORSE_SPAWN_EGG) {
            Horse horse = (Horse) loc.getWorld().spawnEntity(loc, EntityType.HORSE);
            horse.setTamed(true);
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));

            Material[] armors = {
                    null, // represents no armor
                    Material.DIAMOND_HORSE_ARMOR,
                    Material.GOLDEN_HORSE_ARMOR,
                    Material.IRON_HORSE_ARMOR,
                    Material.LEATHER_HORSE_ARMOR
            };

            Material chosen = armors[random.nextInt(armors.length)];
            if (chosen != null) {
                horse.getInventory().setArmor(new ItemStack(chosen));
            } else {
                horse.getInventory().setArmor(null); // explicitly remove armor just in case
            }

        } else if (item.getType() == Material.CAMEL_SPAWN_EGG) {
            Camel camel = (Camel) loc.getWorld().spawnEntity(loc, EntityType.CAMEL);
            camel.setTamed(true);
            camel.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Player damager = null;

        // Direct melee hit
        if (e.getDamager() instanceof Player player) {
            damager = player;
        }

        // Projectile hit (arrow, snowball, trident, etc.)
        else if (e.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player p) {
                damager = p;
            }
        }

        if (damager == null) {
            return;
        }

        Entity damaged = e.getEntity();

        Game game = gameManager.getGame(e.getEntity().getWorld());
        if (game == null) return;

        // Check if damaged entity has any riders
        for (Entity passenger : damaged.getPassengers()) {
            if (game.getPlayerManager().areSameTeam(damager, passenger)) {
                e.setCancelled(true);
                return;
            }
        }
    }
}


