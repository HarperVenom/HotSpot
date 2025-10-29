package me.harpervenom.hotspot;

import me.harpervenom.hotspot.commands.LobbyCommand;
import me.harpervenom.hotspot.game.GameEventListener;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.game.GameModeEnum;
import me.harpervenom.hotspot.game.map.MapManager;
import me.harpervenom.hotspot.lobby.LobbyEventListener;
import me.harpervenom.hotspot.lobby.LobbyManager;
import me.harpervenom.hotspot.menu.MenuEventListener;
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

        saveDefaultConfig();

        LobbyManager lobbyManager = new LobbyManager();
        PlayerManager playerManager = new PlayerManager();
        QueueManager queueManager = new QueueManager(playerManager);
        GameManager gameManager = new GameManager();
        MenuManager menuManager = new MenuManager(playerManager, queueManager, gameManager);

        lobbyManager.addListener(menuManager);

        queueManager.addListener(menuManager);
        queueManager.addListener(gameManager);

        gameManager.addListener(lobbyManager);
        gameManager.addListener(queueManager);
        gameManager.addListener(menuManager);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new LobbyEventListener(lobbyManager), this);
        getServer().getPluginManager().registerEvents(new MenuEventListener(menuManager, playerManager), this);
        getServer().getPluginManager().registerEvents(new QueueEventListener(queueManager), this);
        getServer().getPluginManager().registerEvents(new GameEventListener(gameManager), this);

        queueManager.createQueue(GameModeEnum.NORMAL);

        LobbyCommand lobbyCommand = new LobbyCommand(lobbyManager);
        getCommand("lobby").setExecutor(lobbyCommand);
        getCommand("spawn").setExecutor(lobbyCommand);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        String version = getPluginMeta().getVersion();
        event.motd(text("HotSpot v" + version, NamedTextColor.GRAY));
    }
}
