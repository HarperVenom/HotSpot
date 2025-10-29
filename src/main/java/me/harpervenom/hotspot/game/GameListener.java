package me.harpervenom.hotspot.game;

import org.bukkit.entity.Player;

public interface GameListener {
    default void onGameStart(Game game) {};
    default void onGameEnd(Game game) {};
    default void onGamesUpdate() {};
    default void onPlayerEnterGame(Player player) {};
}
