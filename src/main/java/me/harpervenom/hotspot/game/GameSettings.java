package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.map.MapData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class GameSettings {

    private final Component name;
    private final Material queueMaterial, gameMaterial;
    private MapData mapData;
    private final int maxTeamPlayers;
    private final boolean isCustomTeams;
    private final boolean canJoinMidGame;
    private final int minPlayers;

    public GameSettings(Component name, Material queueMaterial, Material gameMaterial,
                        int maxTeamPlayers, boolean isCustomTeams, boolean canJoinMidGame, int minPlayers) {
        this.name = name;
        this.queueMaterial = queueMaterial;
        this.gameMaterial = gameMaterial;
        this.maxTeamPlayers = maxTeamPlayers;
        this.isCustomTeams = isCustomTeams;
        this.canJoinMidGame = canJoinMidGame;
        this.minPlayers = minPlayers;
    }

    public GameSettings(Component name, Material queueMaterial, Material gameMaterial, boolean isCustomTeams) {
        this.name = name;
        this.queueMaterial = queueMaterial;
        this.gameMaterial = gameMaterial;
        this.maxTeamPlayers = 15;
        this.isCustomTeams = isCustomTeams;
        this.canJoinMidGame = true;
        this.minPlayers = 2;
    }

    public Component getName() {
        return name;
    }
    public Material getQueueMaterial() {
        return queueMaterial;
    }
    public Material getGameMaterial() {
        return gameMaterial;
    }
    public int getMaxTeamPlayers() {
        return maxTeamPlayers;
    }
    public int getMaxPlayers() {
        return maxTeamPlayers * 2;
    }
    public boolean isCustom() {
        return isCustomTeams;
    }
    public boolean isCanJoinMidGame() {
        return canJoinMidGame;
    }
    public int getMinPlayers() {
        return minPlayers;
    }
    public void setMapData(MapData mapData) {
        this.mapData = mapData;
    }
    public MapData getMapData() {
        return mapData;
    }
}
