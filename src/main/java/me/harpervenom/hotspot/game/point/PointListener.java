package me.harpervenom.hotspot.game.point;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PointListener implements Listener {

    private final GameManager gameManager;

    public PointListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPointCapture(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();

        Game game = gameManager.getGame(block.getWorld());
        if (game == null) return;

        Point point = game.getPointManager().getPoint(block);
        if (point == null) return;

        e.setCancelled(true);

        game.getPointManager().capture(point, player);
    }
}
