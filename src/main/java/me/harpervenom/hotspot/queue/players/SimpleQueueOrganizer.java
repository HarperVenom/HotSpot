package me.harpervenom.hotspot.queue.players;

import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import me.harpervenom.hotspot.queue.players.team.QueueTeamManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SimpleQueueOrganizer implements QueuePlayerOrganizer {

    private final GameQueue queue;
    private final List<Player> players = new ArrayList<>();

    public SimpleQueueOrganizer(GameQueue queue) {
        this.queue = queue;
    }

    @Override
    public boolean canAccept(Player p, QueueTeam team) {
        return (players.size() < queue.getSettings().getMaxPlayers());
    }

    @Override
    public boolean addPlayer(Player p) {
        if (players.size() >= queue.getSettings().getMaxPlayers()) return false;
        players.add(p);
        return true;
    }

    @Override
    public boolean addPlayerToTeam(Player p, QueueTeam t) {
        return addPlayer(p); // ignore team in classic mode
    }

    @Override
    public void removePlayer(Player p) {
        players.remove(p);
    }

    @Override
    public List<Player> getAllPlayers() {
        return players;
    }
}
