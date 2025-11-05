package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.team.GameTeam;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

import static me.harpervenom.hotspot.utils.Utils.text;

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
    public Component getName() {
        return text(getPlayer().getName(), gameTeam.getColor());
    }
//    public Component getName() {
//        return text(gamePlayer.getPlayer().getName(), gameTeam.getColor());
//    }
}
