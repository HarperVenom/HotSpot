package me.harpervenom.hotspot.game;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import static net.kyori.adventure.text.Component.text;

public enum GameModeEnum {
    NORMAL(text("[НОРМАЛ]"), Material.WHITE_STAINED_GLASS, Material.WHITE_CONCRETE, 30);

//    private final Component name;
//    private final Material queueMaterial, gameMaterial;
    private final GameSettings gameSettings;

    GameModeEnum(Component name, Material queueMaterial, Material gameMaterial, int maxPlayers) {
//        this.name = name;
//        this.queueMaterial = queueMaterial;
//        this.gameMaterial = gameMaterial;
        gameSettings = new GameSettings(name, queueMaterial, gameMaterial, maxPlayers);
    }

//    public Component getName() {
//        return name;
//    }
//    public Material getQueueMaterial() {
//        return queueMaterial;
//    }
//    public Material getGameMaterial() {
//        return gameMaterial;
//    }
    public GameSettings getSettings() {
        return gameSettings;
    }
}
