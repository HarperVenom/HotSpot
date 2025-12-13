package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.map.MapData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class GameSettings {

    private final Component name;
    private final Material queueMaterial, gameMaterial;
    private MapData mapData;
    private final int maxTeamPlayers;
    private boolean canChooseTeam;
    private final boolean isCustom;
    private final boolean canJoinMidGame;
    private final int minPlayers;

    public GameSettings(Component name, Material queueMaterial, Material gameMaterial,
                        int maxTeamPlayers, boolean canChooseTeam, boolean isCustom, boolean canJoinMidGame, int minPlayers) {
        this.name = name;
        this.queueMaterial = queueMaterial;
        this.gameMaterial = gameMaterial;
        this.maxTeamPlayers = maxTeamPlayers;
        this.canChooseTeam = canChooseTeam;
        this.isCustom = isCustom;
        this.canJoinMidGame = canJoinMidGame;
        this.minPlayers = minPlayers;
    }

    public GameSettings(Component name, Material queueMaterial, Material gameMaterial, boolean isCustom) {
        this.name = name;
        this.queueMaterial = queueMaterial;
        this.gameMaterial = gameMaterial;
        this.maxTeamPlayers = 15;
        this.canChooseTeam = false;
        this.isCustom = isCustom;
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
        return isCustom;
    }
    public void setCanChooseTeam(boolean canChooseTeam) {
        this.canChooseTeam = canChooseTeam;
    }
    public boolean canChooseTeam() {
        return canChooseTeam;
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
