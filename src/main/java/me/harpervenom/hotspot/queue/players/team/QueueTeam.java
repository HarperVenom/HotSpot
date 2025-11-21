package me.harpervenom.hotspot.queue.players.team;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class QueueTeam {

    private final NamedTextColor color;
    private final String name;
    private final List<Player> players = new ArrayList<>();

    public QueueTeam(NamedTextColor color, String name) {
        this.color = color;
        this.name = name;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return switch (color.toString().toUpperCase()) {
            case "RED" -> Material.RED_WOOL;
            case "BLUE" -> Material.BLUE_WOOL;
            default -> Material.WHITE_CONCRETE;
        };
    }
}
