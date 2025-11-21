package me.harpervenom.hotspot.queue.players;

import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import me.harpervenom.hotspot.queue.players.team.QueueTeamManager;
import org.bukkit.entity.Player;

import java.util.List;

public class TeamQueueOrganizer implements QueuePlayerOrganizer {
    private final QueueTeamManager teamManager;

    public TeamQueueOrganizer(GameQueue queue) {
        this.teamManager = new QueueTeamManager(queue);
        teamManager.createTeams();
    }

    @Override
    public void addPlayer(Player p) {
        // Optional: could show team selection UI here
        teamManager.addPlayer(p, teamManager.getTeams().getFirst());
    }

    @Override
    public void addPlayerToTeam(Player p, QueueTeam team) {
        teamManager.addPlayer(p, team);  // <-- This is the key
    }

    @Override
    public void removePlayer(Player p) {
        teamManager.removePlayer(p);
    }

    @Override
    public List<Player> getAllPlayers() {
        return teamManager.getPlayers();
    }

    public QueueTeamManager getTeamManager() {
        return teamManager;
    }
}
