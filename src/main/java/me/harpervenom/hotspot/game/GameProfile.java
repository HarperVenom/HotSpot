package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.team.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GameProfile {

    private final UUID playerId;
    private GameTeam gameTeam;
    private boolean isConnected = true;

    public GameProfile(Player player) {
        this.playerId = player.getUniqueId();
    }

    public void setTeam(GameTeam gameTeam) {
        this.gameTeam = gameTeam;
    }
    public GameTeam getTeam() {
        return gameTeam;
    }
    public Player getPlayer() {
        return Bukkit.getPlayer(playerId);
    }
    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
    public boolean isConnected() {
        return isConnected;
    }
//    public Component getName() {
//        return text(gamePlayer.getPlayer().getName(), gameTeam.getColor());
//    }
}
