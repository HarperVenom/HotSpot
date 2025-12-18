package me.harpervenom.hotspot.statistics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class StatsListener implements Listener {

    private final StatsManager statsManager;

    public StatsListener(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        statsManager.loadStats(player.getUniqueId());
        statsManager.updateTab();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            statsManager.updateTab();
        }, 1);
    }
}
