package me.harpervenom.hotspot;

import me.harpervenom.hotspot.game.GameModeEnum;
import me.harpervenom.hotspot.lobby.LobbyEventListener;
import me.harpervenom.hotspot.lobby.LobbyManager;
import me.harpervenom.hotspot.menu.MenuListener;
import me.harpervenom.hotspot.menu.MenuManager;
import me.harpervenom.hotspot.player.PlayerManager;
import me.harpervenom.hotspot.queue.QueueEventListener;
import me.harpervenom.hotspot.queue.QueueManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static me.harpervenom.hotspot.utils.Utils.text;

public final class HotSpot extends JavaPlugin implements Listener {

    public static HotSpot plugin;

    @Override
    public void onEnable() {
        plugin = this;

        LobbyManager lobbyManager = new LobbyManager();
        PlayerManager playerManager = new PlayerManager();
        QueueManager queueManager = new QueueManager(playerManager);
        MenuManager menuManager = new MenuManager(playerManager, queueManager);

        lobbyManager.addListener(menuManager);

        queueManager.addListener(menuManager);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new LobbyEventListener(lobbyManager), this);
        getServer().getPluginManager().registerEvents(new MenuListener(menuManager, playerManager), this);
        getServer().getPluginManager().registerEvents(new QueueEventListener(queueManager), this);

        queueManager.createQueue(GameModeEnum.NORMAL);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        String version = getPluginMeta().getVersion();
        event.motd(text("HotSpot v" + version, NamedTextColor.GRAY));
    }
}
