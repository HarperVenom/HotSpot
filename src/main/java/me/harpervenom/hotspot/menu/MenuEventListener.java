package me.harpervenom.hotspot.menu;

import me.harpervenom.hotspot.lobby.LobbyManager;
import me.harpervenom.hotspot.menu.components.Window;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.menu.components.Window.openedWindow;

public class MenuEventListener implements Listener {

    private final MenuManager manager;
    private final LobbyManager lobbyManager;

    private final List<Player> justClicked = new ArrayList<>();

    public MenuEventListener(MenuManager manager, LobbyManager lobbyManager) {
        this.manager = manager;
        this.lobbyManager = lobbyManager;
    }

    @EventHandler
    public void onButtonInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (!lobbyManager.isLobby(player.getWorld())) return;

        if (!e.getAction().isRightClick() || e.getHand() != EquipmentSlot.HAND) return;

        if (hasJustClicked(player)) return;

        if (manager.getLobbyController().handleHandClick(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryButtonClick(InventoryClickEvent e) {
        Player player = getPlayer(e);

        Window window = openedWindow.get(player);
        if (window == null) return;

        e.setCancelled(true);

        window.click(e.getRawSlot(), player, e.isShiftClick());
    }

    @EventHandler
    public void onWindowClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();

        Window window = openedWindow.get(player);
        if (window == null) return;

        window.close(player, false);
        openedWindow.remove(player);
    }

    private boolean hasJustClicked(Player player) {
        if (justClicked.contains(player)) return true;
        justClicked.add(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            justClicked.remove(player);
        }, 1);
        return false;
    }

    private Player getPlayer(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            return p;
        }
        return null;
    }
}
