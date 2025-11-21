package me.harpervenom.hotspot.queue.players;

import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import org.bukkit.entity.Player;

import java.util.List;

public interface QueuePlayerOrganizer {
    void addPlayer(Player p);                 // generic entry, optional
    void removePlayer(Player p);
    List<Player> getAllPlayers();

    // New:
    void addPlayerToTeam(Player p, QueueTeam team);
}
