package me.harpervenom.hotspot.queue;

import me.harpervenom.hotspot.player.GamePlayer;
import me.harpervenom.hotspot.player.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QueueEventListener implements Listener {

    private final QueueManager queueManager;

    public QueueEventListener(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        queueManager.removePlayerFromQueue(player);
    }
}
