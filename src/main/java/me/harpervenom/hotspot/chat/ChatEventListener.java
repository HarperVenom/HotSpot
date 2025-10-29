package me.harpervenom.hotspot.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class ChatEventListener implements Listener {


//    @EventHandler
//    public void onMessage(AsyncChatEvent e) {
//        Player player = e.getPlayer();
////        if (!manager.isLobby(player.getWorld())) return;
//        e.setCancelled(true);
//
//
//        manager.handleChatMessage(e.message(), player);
//    }
//
//    public void showMessage(Component message) {
//        for (Player player : world.getPlayers()) {
//            player.sendMessage(message);
//        }
//        plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message));
//    }

    public void showMessage(List<Player> players, Component message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
        plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message));
    }
}
