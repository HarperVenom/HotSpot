package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.map.MapData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class GameSettings {

    private final Component name;
    private final Material queueMaterial, gameMaterial;
    private MapData mapData;
    private int maxPlayers;

    public GameSettings(Component name, Material queueMaterial, Material gameMaterial, int maxPlayers) {
        this.name = name;
        this.queueMaterial = queueMaterial;
        this.gameMaterial = gameMaterial;
        this.maxPlayers = maxPlayers;
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
}
