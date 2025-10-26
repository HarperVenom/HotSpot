package me.harpervenom.hotspot.lobby;

import org.bukkit.entity.Player;

public interface LobbyListener {
    default void onLobby(Player player) {}
}
