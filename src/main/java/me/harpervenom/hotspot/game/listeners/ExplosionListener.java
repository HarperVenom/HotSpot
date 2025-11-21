package me.harpervenom.hotspot.game.listeners;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import org.bukkit.event.Listener;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.game.map.GameMap.immuneMaterials;
import static me.harpervenom.hotspot.game.vault.loot.CustomItems.tntId;
import static me.harpervenom.hotspot.utils.Utils.getItemId;

public class ExplosionListener implements Listener {

    private final GameManager gameManager;

    public ExplosionListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    static HashMap<UUID, Player> explosions = new HashMap<>();

//    public static final Set<UUID> cooldown = new HashSet<>();

//    public static void cooldown(Player player) {
//        if (cooldown.contains(player.getUniqueId())) return;
//        cooldown.add(player.getUniqueId());
//        Bukkit.getScheduler().runTaskLater(plugin, () -> {
//            cooldown.remove(player.getUniqueId());
//        }, 5);
//    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
    }

//    @EventHandler
//    public void onTNTInteract(PlayerInteractEvent e) {
//        Player player = e.getPlayer();
//        Action action = e.getAction();
//
//        ItemStack item = e.getItem();
//
//        if (!tntId.equals(getItemId(item))) return;
////        if (isOnCooldown(player)) return;
//
//        if (action == Action.RIGHT_CLICK_BLOCK && canPlaceOn(e.getClickedBlock())) {
//            return;
//        }
//        // Only act on left-clicks (air or block)
//        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;
//
//        e.setCancelled(true);
//
//        item.setAmount(item.getAmount() - 1);
//
//        Location initialPlayerLocation = player.getLocation();
//
//        Bukkit.getScheduler().runTaskLater(plugin, () -> {
//            Location newPlayerLocation = player.getLocation();
//
//            Vector movementVector = newPlayerLocation.toVector().subtract(initialPlayerLocation.toVector()).multiply(0.5);
//            Vector direction = player.getLocation().getDirection().normalize();
//
//            Location tntLoc = player.getEyeLocation().add(direction.clone().multiply(0.5));
//            TNTPrimed tnt = player.getWorld().spawn(tntLoc, TNTPrimed.class);
//            tnt.setFuseTicks(50);
//
//            // Add player's movement vector to forward throw direction
//            Vector tntVelocity = direction.multiply(0.15).add(movementVector);
//            tnt.setVelocity(tntVelocity);
//
//            tnt.getWorld().playSound(tntLoc, Sound.ENTITY_TNT_PRIMED, 1, 1);
//            player.swingMainHand();
//
//            explosions.put(tnt.getUniqueId(), player);
//
//            Bukkit.getScheduler().runTaskLater(plugin, () -> {
//                explosions.remove(tnt.getUniqueId());
//            }, 80);
//        }, 1);
//    }

    private boolean canPlaceOn(Block clickedBlock) {
        if (clickedBlock == null) return false;

        // Example of placeable surfaces, you can expand this
        Material type = clickedBlock.getType();
        return type.isSolid();
//                && isInteractable(type);
    }

    @EventHandler
    public void EntityExplode(EntityExplodeEvent e) {
        Location loc = e.getEntity().getLocation();
        List<Block> blocks = e.blockList();
        updateBlockList(loc, blocks);
        e.setYield(0f);
    }

    @EventHandler
    public void onExplosionDamage(EntityDamageByEntityEvent e) {
        Bukkit.broadcastMessage("3");
        UUID tntId = e.getDamager().getUniqueId();

        if (explosions.containsKey(tntId)) {
            Player exploder = explosions.get(tntId);

            Game game = gameManager.getGame(e.getEntity().getWorld());
            if (game == null) return;
            game.getDamageManager().handleExplosionDamage(e, exploder);
        }
    }

    public void updateBlockList(Location loc, List<Block> blocks) {
        Game game = gameManager.getGame(loc.getWorld());
        if (game == null) return;

        blocks.removeIf(block -> immuneMaterials.contains(block.getType()) || !game.getMap().canBrake(block));
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        e.getEntity().setInvulnerable(true);
    }


//    public static void createExplosion(Player player, float power) {
//        explosions.put(player.getUniqueId(), player);
//
//        Bukkit.getScheduler().runTaskLater(plugin, () -> {
//            explosions.remove(player.getUniqueId());
//        }, 80);
//
//        player.getWorld().createExplosion(player.getLocation(), power, false, false, player);
//    }
}

