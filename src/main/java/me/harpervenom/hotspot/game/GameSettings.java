package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.map.MapData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class GameSettings {

    private final Component name;
    private final Material queueMaterial, gameMaterial;
    private MapData mapData;
    private final int maxPlayers;
    private final boolean isCustomTeams;

    public GameSettings(Component name, Material queueMaterial, Material gameMaterial, int maxPlayers, boolean isCustomTeams) {
        this.name = name;
        this.queueMaterial = queueMaterial;
        this.gameMaterial = gameMaterial;
        this.maxPlayers = maxPlayers;
        this.isCustomTeams = isCustomTeams;
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
    public int getMaxPlayers() {
        return maxPlayers;
    }
    public boolean isCustom() {
        return isCustomTeams;
    }
    public void setMapData(MapData mapData) {
        this.mapData = mapData;
    }
    public MapData getMapData() {
        return mapData;
    }
}
