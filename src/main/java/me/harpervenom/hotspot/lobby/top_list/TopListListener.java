package me.harpervenom.hotspot.lobby.top_list;

import me.harpervenom.hotspot.lobby.LobbyManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import static me.harpervenom.hotspot.lobby.top_list.HoloText.removeTaggedTextDisplays;

public class TopListListener implements Listener {

    private final TopListManager topListManager;
    private final LobbyManager lobbyManager;

    public TopListListener(TopListManager topListManager, LobbyManager lobbyManager) {
        this.topListManager = topListManager;
        this.lobbyManager = lobbyManager;
    }

    @EventHandler
    public void onServerStart(ServerLoadEvent e) {
        World lobby = lobbyManager.getLobbyWorld();

        removeTaggedTextDisplays();

        topListManager.loadLists();

//        int x = plugin.getConfig().getInt("info_holo.x");
//        int y = plugin.getConfig().getInt("info_holo.y");
//        int z = plugin.getConfig().getInt("info_holo.z");
//
//        Location serverInfoLoc = lobby.getBlockAt(x, y + 3, z).getLocation();
//
//        HoloText serverInfo = new HoloText(serverInfoLoc);
//        holoTexts.add(serverInfo);
//
//        serverInfo.addLine(text("Добро пожаловать!", NamedTextColor.WHITE));
//        serverInfo.addLineSpacing(0.10);
//        serverInfo.addLine(text("Цель игры - захватить и удерживать точки.", NamedTextColor.WHITE));
//        serverInfo.addLineSpacing(0.10);
//        serverInfo.addLine(text("Собирайте лут с раздатчиков и сражайтесь.", NamedTextColor.WHITE));
//        serverInfo.addLineSpacing(0.10);
//        serverInfo.addLine(text("За нанесение урона вы получаете уровень", NamedTextColor.WHITE));
//        serverInfo.addLine(text("который можно потратить в магазине.", NamedTextColor.WHITE));
//        serverInfo.addLineSpacing(0.10);
//        serverInfo.addLine(text("Не дайте счету упасть до нуля.", NamedTextColor.WHITE));

//        x = plugin.getConfig().getInt("damage_top.x");
//        y = plugin.getConfig().getInt("damage_top.y");
//        z = plugin.getConfig().getInt("damage_top.z");
//        Location dealtDamageListLocation = lobby.getBlockAt(x, y + 3, z).getLocation();
//
//        x = plugin.getConfig().getInt("points_top.x");
//        y = plugin.getConfig().getInt("points_top.y");
//        z = plugin.getConfig().getInt("points_top.z");
//        Location capturedPointsListLocation = lobby.getBlockAt(x, y + 3, z).getLocation();
//
//        x = plugin.getConfig().getInt("kills_top.x");
//        y = plugin.getConfig().getInt("kills_top.y");
//        z = plugin.getConfig().getInt("kills_top.z");
//        Location killsListLocation = lobby.getBlockAt(x, y + 3, z).getLocation();
//
//        dealtDamageList = new TopList(dealtDamageListLocation, TopListType.DAMAGE_DEALT, "Нанесённый Урон", null);
//        pointsCapturedList = new TopList(capturedPointsListLocation, TopListType.POINTS_CAPTURED, "Захваченные Точки", null);
//        killsList = new TopList(killsListLocation, TopListType.KILLS, "Убийства", null);

//        updateStats();

//        Location tgLinkLocation = lobby.getBlockAt(8, 2, 8).getLocation();
//        HoloText tgLink = new HoloText(tgLinkLocation.clone().add(0, -0.2, 0));
//        holoTexts.add(tgLink);
//
//        tgLink.addLine(text("Ссылка в телеграм", NamedTextColor.AQUA));
//        tgLink.addLine(text("*Клик*", NamedTextColor.AQUA));
//        tgLink.addLinkToLastLine("https://t.me/venom_harper_game");
//
//        generateQueue();
    }
}
