package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.map.MapData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class GameSettings {

    private final Component name;
    private final Material queueMaterial, gameMaterial;
    private final int maxTeamPlayers;
    private final boolean isCustom;
    private final int minPlayers;

    private MapData mapData;
    private boolean canChooseTeam;
    private boolean canJoinMidGame;
    private boolean pointsInOrder;

    public GameSettings(Component name, Material queueMaterial, Material gameMaterial,
                        int maxTeamPlayers, boolean canChooseTeam, boolean isCustom,
                        boolean canJoinMidGame, int minPlayers, boolean pointsInOrder) {
        this.name = name;
        this.queueMaterial = queueMaterial;
        this.gameMaterial = gameMaterial;
        this.maxTeamPlayers = maxTeamPlayers;
        this.canChooseTeam = canChooseTeam;
        this.isCustom = isCustom;
        this.canJoinMidGame = canJoinMidGame;
        this.minPlayers = minPlayers;
        this.pointsInOrder = pointsInOrder;
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
        this.pointsInOrder = false;
    }

    public GameSettings(GameSettings other) {
        this.name = other.name;
        this.queueMaterial = other.queueMaterial;
        this.gameMaterial = other.gameMaterial;
        this.mapData = other.mapData;
        this.maxTeamPlayers = other.maxTeamPlayers;
        this.canChooseTeam = other.canChooseTeam;
        this.isCustom = other.isCustom;
        this.canJoinMidGame = other.canJoinMidGame;
        this.minPlayers = other.minPlayers;
        this.pointsInOrder = other.pointsInOrder;
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

    public void setPointsInOrder(boolean pointsInOrder) {
        this.pointsInOrder = pointsInOrder;
    }
    public boolean isPointsInOrder() {
        return pointsInOrder;
    }
}
