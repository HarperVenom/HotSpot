package me.harpervenom.hotspot.lobby.top_list;

import me.harpervenom.hotspot.lobby.LobbyManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import static me.harpervenom.hotspot.lobby.top_list.HoloText.cleanDisplays;

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

        cleanDisplays(lobby);

        topListManager.loadLists();
    }
}
