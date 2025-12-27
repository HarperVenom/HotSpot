package me.harpervenom.hotspot.lobby;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import me.harpervenom.hotspot.utils.ChangelogUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.text;


public class LobbyEventListener implements Listener {

    private final LobbyManager manager;

    public LobbyEventListener(LobbyManager manager) {
        this.manager = manager;

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            World world = manager.getLobbyWorld();
            if (world == null) return;

            double killBelow = manager.getSpawnLoc().getY() - 30; // use Optional or null-safe
            if (Double.isNaN(killBelow)) return;

            // Snapshot + filter in one go
            world.getPlayers().stream()
                    .filter(Player::isOnline)
                    .filter(p -> p.getLocation().getY() < killBelow)
                    .forEach(player -> {
                        try {
                            manager.sendToLobby(player);
                        } catch (Exception e) {
                            plugin.getLogger().severe("Failed to send " + player.getName() + " to lobby: " + e);
                        }
                    });
        }, 0L, 5L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        manager.sendToLobby(player);
        updateTabMenu();

        String version = plugin.getPluginMeta().getVersion();
        player.sendMessage(text("HotSpot v" + version, NamedTextColor.GRAY));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        manager.sendToLobby(e.getPlayer());
        Bukkit.getScheduler().runTaskLater(plugin, LobbyEventListener::updateTabMenu, 1);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!manager.isLobby(e.getPlayer().getWorld())) return;
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
    }

    @EventHandler
    public void InventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (!manager.isLobby(player.getWorld())) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame && event.getDamager() instanceof Player) {
            event.setCancelled(true); // Prevents removing the item or breaking the frame
        }
    }

    @EventHandler
    public void ItemDrop(PlayerDropItemEvent e) {
        if (!manager.isLobby(e.getPlayer().getWorld())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void SwitchHands(PlayerSwapHandItemsEvent e) {
        if (!manager.isLobby(e.getPlayer().getWorld())) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void PlayerDamage(EntityDamageEvent e) {
        if (!manager.isLobby(e.getEntity().getWorld())) return;
        if (!(e.getEntity() instanceof Player)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void Hunger(FoodLevelChangeEvent e){
        if (!manager.isLobby(e.getEntity().getWorld())) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerOutOfTheWorld(EntityDamageEvent e) {
        if (!manager.isLobby(e.getEntity().getWorld())) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.VOID) return;
        if (!(e.getEntity() instanceof Player player)) return;

        manager.sendToLobby(player);
    }

    @EventHandler
    public void onDialog( PlayerCustomClickEvent e) {
        Bukkit.broadcastMessage("hey");
    }

    @EventHandler
    public void onAchievement(PlayerAdvancementDoneEvent e) {
        e.message(null);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        updateTabMenu();
    }

    public static void updateTabMenu() {
        List<Player> players = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());

        for (Player player : players) {
            World playerWorld = player.getWorld();

            for (Player other : players) {
                if (player.equals(other)) {
                    continue;
                }

                if (other.getWorld().equals(playerWorld)) {
                    player.listPlayer(other);
                } else {
                    player.unlistPlayer(other);
                }
            }

            player.sendPlayerListFooter(text(" Онлайн: ", NamedTextColor.GRAY)
                    .append(text(players.size() + " ")));
        }
    }

    public void sendPatchNotes(Player player) {
        ChangelogUtil.sendChangelog(player);
    }

    @EventHandler
    public void onButtonPress(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!manager.isLobby(player.getWorld())) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        Material type = e.getClickedBlock().getType();

        // Check if it's any type of button
        if (type.name().endsWith("_BUTTON")) {

            sendPatchNotes(player);
        }
    }
}
