package me.harpervenom.hotspot.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import static net.kyori.adventure.text.Component.text;

public enum GameModeEnum {
    NORMAL(text("[НОРМАЛ]"),
            Material.WHITE_STAINED_GLASS, Material.WHITE_CONCRETE,
            15, false, true, 2),
    RANKED(text("[РАНГ]", NamedTextColor.GOLD),
            Material.ORANGE_STAINED_GLASS, Material.ORANGE_CONCRETE,
            5, false, false, 10),
    CUSTOM(text("[КАСТОМ]", NamedTextColor.LIGHT_PURPLE),
            Material.PURPLE_STAINED_GLASS, Material.PURPLE_CONCRETE,
            15, true, false, 2),
    ;

    private final GameSettings gameSettings;

    GameModeEnum(Component name, Material queueMaterial, Material gameMaterial, boolean isCustomTeams) {
        gameSettings = new GameSettings(name, queueMaterial, gameMaterial, isCustomTeams);
    }

    GameModeEnum(Component name, Material queueMaterial, Material gameMaterial, int maxTeamPlayers,
                 boolean isCustomTeams, boolean canJoinMidGam, int minPlayers) {
        gameSettings = new GameSettings(name, queueMaterial, gameMaterial, maxTeamPlayers, isCustomTeams, canJoinMidGam, minPlayers);
    }

    public GameSettings getSettings() {
        return gameSettings;
    }
}
