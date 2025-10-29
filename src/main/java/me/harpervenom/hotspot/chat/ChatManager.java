package me.harpervenom.hotspot.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class ChatManager {

    public ChatManager() {

    }

    public void sendMessage(Player player, String message) {

    }

    public void sendMessage(List<Player> players, Component message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
        plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message));
    }
}
