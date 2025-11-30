package me.harpervenom.hotspot.queue.players;

import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import org.bukkit.entity.Player;

import java.util.List;

public interface QueuePlayerOrganizer {
    boolean canAccept(Player p, QueueTeam team);
    boolean addPlayer(Player p);
    boolean addPlayerToTeam(Player p, QueueTeam team);// generic entry, optional
    void removePlayer(Player p);
    List<Player> getAllPlayers();
}
