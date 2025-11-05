package me.harpervenom.hotspot.game.listeners;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class DeathListener implements Listener {

    private final GameManager gameManager;

    private static final List<UUID> justDied = new ArrayList<>();

    public DeathListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();
        Game game = gameManager.getGame(player.getWorld());
        if (game == null) return;

        game.getDeathHandler().handlePlayerDeath(e, player);

        justDied.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            justDied.remove(player.getUniqueId());
        }, 20);
    }

    @EventHandler
    public void onSpectate(PlayerStartSpectatingEntityEvent e) {
        Player player = e.getPlayer();
        if (justDied.contains(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerOutOfTheWorld(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.VOID) return;
        if (!(e.getEntity() instanceof Player player)) return;
        player.setHealth(0.1);
        handleVoidTeleport(player);
        player.playSound(player, Sound.ENTITY_PLAYER_HURT, 1, 1);
    }

    private void handleVoidTeleport(Player player) {
        if (player.getLocation().getY() < -70) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location newLocation = player.getLocation().clone();
                newLocation.setY(10);
                newLocation.setPitch(45);
                player.teleport(newLocation);
            }, 1L);
        }
    }
}
