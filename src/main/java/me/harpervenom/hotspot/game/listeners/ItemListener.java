package me.harpervenom.hotspot.game.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class ItemListener implements Listener {

    private final List<UUID> interacting = new ArrayList<>();

//    @EventHandler
//    public void onInteract(PlayerInteractEvent e) {
//        Player player = e.getPlayer();
//
//        if (interacting.contains(player.getUniqueId())) {
//            e.setCancelled(true);
//            return;
//        }
//
//        interacting.add(player.getUniqueId());
//
//        Bukkit.getScheduler().runTaskLater(plugin, () -> {
//            interacting.remove(player.getUniqueId());
//        }, 1);
//    }
}
