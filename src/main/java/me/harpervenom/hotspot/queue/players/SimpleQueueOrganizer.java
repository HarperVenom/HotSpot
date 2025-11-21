package me.harpervenom.hotspot.queue.players;

import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleQueueOrganizer implements QueuePlayerOrganizer {
    private final List<Player> players = new ArrayList<>();

    @Override
    public void addPlayer(Player p) {
        players.add(p);
    }

    @Override
    public void addPlayerToTeam(Player p, QueueTeam t) {
        addPlayer(p); // ignore team in classic mode
    }

    @Override
    public void removePlayer(Player p) {
        players.remove(p);
    }

    @Override
    public List<Player> getAllPlayers() {
        return Collections.unmodifiableList(players);
    }
}
