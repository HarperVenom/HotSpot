package me.harpervenom.hotspot.utils.scoreboard;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.*;

public final class PacketSidebar {

    private final String baseId;
    private final Component title;

    private final Map<String, SidebarGroup> groups = new HashMap<>();

    public PacketSidebar(String baseId, Component title) {
        this.baseId = baseId;
        this.title = title;
    }

    public void update(String key, Set<Player> viewers, List<Component> lines) {
        SidebarGroup group = groups.computeIfAbsent(
                key,
                k -> new SidebarGroup(baseId + "_" + k, title)
        );

        group.updateViewers(viewers);
        group.send(lines);
    }

    public void remove(String key) {
        SidebarGroup group = groups.remove(key);
        if (group != null) {
            group.removeAll();
        }
    }

    public void clearAll() {
        for (SidebarGroup group : groups.values()) {
            group.removeAll();
        }
        groups.clear();
    }
}

