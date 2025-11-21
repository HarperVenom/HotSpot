package me.harpervenom.hotspot.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import static net.kyori.adventure.text.Component.text;

public enum GameModeEnum {
    NORMAL(text("[НОРМАЛ]"), Material.WHITE_STAINED_GLASS, Material.WHITE_CONCRETE, 30, false),
    CUSTOM(text("[КАСТОМ]", NamedTextColor.LIGHT_PURPLE),
            Material.PURPLE_STAINED_GLASS, Material.PURPLE_CONCRETE, 30, true)
    ;

    private final GameSettings gameSettings;

    GameModeEnum(Component name, Material queueMaterial, Material gameMaterial, int maxPlayers, boolean isCustomTeams) {
        gameSettings = new GameSettings(name, queueMaterial, gameMaterial, maxPlayers, isCustomTeams);
    }

    public GameSettings getSettings() {
        return gameSettings;
    }
}
