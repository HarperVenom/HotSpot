package me.harpervenom.hotspot.queue.players.team;

import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.queue.QueueListener;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueueTeamManager {

    private final GameQueue queue;
    private final List<QueueTeam> teams = new ArrayList<>();

    public QueueTeamManager(GameQueue queue) {
        this.queue = queue;
    }

    public void createTeams() {
        createTeam(NamedTextColor.RED, "Красные");
        createTeam(NamedTextColor.BLUE, "Синие");
    }

    private void createTeam(NamedTextColor color, String name) {
        teams.add(new QueueTeam(color, name));
    }

    public void addPlayer(Player player, QueueTeam team) {
        team.addPlayer(player);
    }

    public void removePlayer(Player player) {
        QueueTeam team = getTeam(player);
        if (team != null) {
            team.removePlayer(player);
        }
    }

    public List<QueueTeam> getTeams() {
        return teams;
    }

    public QueueTeam getTeam(Player player) {
        for (QueueTeam team : teams) {
            if (team.getPlayers().contains(player)) {
                return team;
            }
        }
        return null;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(
                teams.stream()
                        .flatMap(team -> team.getPlayers().stream())
                        .toList()
        );
    }
}

