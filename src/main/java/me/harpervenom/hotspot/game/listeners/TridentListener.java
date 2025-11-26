package me.harpervenom.hotspot.game.listeners;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class TridentListener implements Listener {

    private static final Map<Trident, Player> shotTridents = new HashMap<>();
    private static final Map<Trident, BukkitTask> returnTasks = new HashMap<>();

    static {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Map<Trident, Player> copy = new HashMap<>(shotTridents);

            for (Map.Entry<Trident, Player> map : copy.entrySet()) {
                Trident trident = map.getKey();
                Player p = map.getValue();

                if (trident.isDead() || !trident.isValid()) {
                    shotTridents.remove(trident);
                    returnTasks.remove(trident);

                    trident.remove(); // Remove from world
                    p.getInventory().addItem(trident.getItemStack());
                    p.playSound(p, Sound.ITEM_TRIDENT_RETURN, 0.4f, 1.5f);
                }
            }
        }, 0, 20);
    }

//    private final GameManager gameManager;
//
//    public TridentListener(GameManager gameManager) {
//        this.gameManager = gameManager;
//    }

    @EventHandler
    public void onPickUp(PlayerPickupArrowEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getArrow() instanceof Trident trident)) return;

        shotTridents.remove(trident);
        returnTasks.remove(trident);
    }

    @EventHandler
    public void onTridentShoot(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof Trident trident) {
            if (trident.getShooter() instanceof Player p) {
                shotTridents.put(trident, p);

                // Schedule return task
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    shotTridents.remove(trident);
                    returnTasks.remove(trident);

                    if (!trident.isDead() && trident.isValid() && trident.getLocation().getWorld() != null) {
                        trident.remove(); // Remove from world
                        p.getInventory().addItem(trident.getItemStack());
                        p.playSound(p, Sound.ITEM_TRIDENT_RETURN, 0.4f, 1.5f);
                    }
                }, 15 * 20);

                returnTasks.put(trident, task);
            }
        }
    }

    public static void clearTridentTasksByGame(Game game) {
        Iterator<Map.Entry<Trident, BukkitTask>> iterator = returnTasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Trident, BukkitTask> entry = iterator.next();
            Trident trident = entry.getKey();
            BukkitTask task = entry.getValue();

            World tridentWorld = trident.getWorld();
            if (tridentWorld.equals(game.getMap().getWorld())) {
                if (task != null) task.cancel();
                iterator.remove(); // Safe removal during iteration
                shotTridents.remove(trident); // Also remove from shotTridents
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player deadPlayer = e.getEntity();

        // Remove all tridents and cancel tasks for this player
        shotTridents.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(deadPlayer)) {
                Trident trident = entry.getKey();
                if (trident != null && trident.isValid()) {
                    trident.remove();
                }

                BukkitTask task = returnTasks.remove(trident);
                if (task != null) task.cancel();

                return true;
            }
            return false;
        });
    }
}
