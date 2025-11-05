package me.harpervenom.hotspot.game;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameEventListener implements Listener {

    private final GameManager gameManager;

    public GameEventListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        World world = e.getFrom();
        Game game = gameManager.getGame(world);
        if (game == null) return;

        Player player = e.getPlayer();
        game.disconnect(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        World world = e.getPlayer().getWorld();
        Game game = gameManager.getGame(world);
        if (game == null) return;

        Player player = e.getPlayer();
        game.disconnect(player);
    }
}
