package me.harpervenom.hotspot;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.harpervenom.hotspot.chat.ChatManager;
import me.harpervenom.hotspot.commands.LobbyCommand;
import me.harpervenom.hotspot.database.Database;
import me.harpervenom.hotspot.game.GameEventListener;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.game.GameModeEnum;
import me.harpervenom.hotspot.game.listeners.*;
import me.harpervenom.hotspot.game.map.MapManager;
import me.harpervenom.hotspot.game.trader.TraderListener;
import me.harpervenom.hotspot.game.vault.VaultListener;
import me.harpervenom.hotspot.game.point.PointListener;
import me.harpervenom.hotspot.lobby.LobbyEventListener;
import me.harpervenom.hotspot.lobby.LobbyManager;
import me.harpervenom.hotspot.lobby.top_list.TopListListener;
import me.harpervenom.hotspot.lobby.top_list.TopListManager;
import me.harpervenom.hotspot.menu.MenuEventListener;
import me.harpervenom.hotspot.menu.MenuManager;
import me.harpervenom.hotspot.placeholder.HotSpotExpansion;
import me.harpervenom.hotspot.queue.QueueEventListener;
import me.harpervenom.hotspot.queue.QueueManager;
import me.harpervenom.hotspot.statistics.StatsListener;
import me.harpervenom.hotspot.statistics.StatsManager;
import me.harpervenom.hotspot.utils.ChangelogUtil;
import me.harpervenom.hotspot.utils.glow.EntityMetadataListener;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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

        PacketEvents.getAPI().init();

        saveDefaultConfig();

        ChangelogUtil.loadChangelog(this);

        Database db = new Database();
        db.init();
        StatsManager statsManager = new StatsManager(db);

        MapManager mapManager = new MapManager();

        LobbyManager lobbyManager = new LobbyManager();
        QueueManager queueManager = new QueueManager(mapManager);
        GameManager gameManager = new GameManager(mapManager, statsManager);
        MenuManager menuManager = new MenuManager(queueManager, gameManager, mapManager, statsManager);
        TopListManager topListManager = new TopListManager(db, lobbyManager);
        statsManager.setGameManager(gameManager);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new HotSpotExpansion(statsManager).register();
        }

        lobbyManager.addListener(menuManager);

        queueManager.addListener(menuManager);
        queueManager.addListener(gameManager);

        gameManager.addListener(lobbyManager);
        gameManager.addListener(queueManager);
        gameManager.addListener(menuManager);
        gameManager.addListener(topListManager);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new LobbyEventListener(lobbyManager), this);
        getServer().getPluginManager().registerEvents(new MenuEventListener(menuManager, lobbyManager), this);
        getServer().getPluginManager().registerEvents(new QueueEventListener(queueManager), this);
        getServer().getPluginManager().registerEvents(new GameEventListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new PointListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new VaultListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new TraderListener(gameManager), this);

        getServer().getPluginManager().registerEvents(new DeathListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new MapListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new DamageListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new BombsListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new RelicListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new SpawnEggListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new PotionListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new ArmorListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new PickaxeListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new SmartWeaponListener(), this);
        getServer().getPluginManager().registerEvents(new TridentListener(), this);
        getServer().getPluginManager().registerEvents(new GeneralListener(), this);
        getServer().getPluginManager().registerEvents(new ChatManager(lobbyManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new StatsListener(statsManager), this);
        getServer().getPluginManager().registerEvents(new TopListListener(topListManager, lobbyManager), this);

        queueManager.createQueue(GameModeEnum.NORMAL);
        queueManager.createQueue(GameModeEnum.RANKED);

        LobbyCommand lobbyCommand = new LobbyCommand(lobbyManager);
        getCommand("lobby").setExecutor(lobbyCommand);
        getCommand("spawn").setExecutor(lobbyCommand);
    }

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        //On Bukkit, calling this here is essential, hence the name "load"
        PacketEvents.getAPI().load();

        PacketEvents.getAPI().getEventManager().registerListener(
                new EntityMetadataListener()
        );

//        PacketEvents.getAPI().getEventManager().registerListener(
//                new EntityMetadataListener(), PacketListenerPriority.NORMAL);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        String version = getPluginMeta().getVersion();
        event.motd(text("HotSpot v" + version, NamedTextColor.GRAY));
    }
}
