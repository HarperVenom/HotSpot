package me.harpervenom.hotspot.game.team;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.game.point.Point;
import me.harpervenom.hotspot.game.point.PointManager;
import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameTeamManager {

    private final Game game;
    private final List<GameTeam> teams = new ArrayList<>();

    public GameTeamManager(Game game) {
        this.game = game;
    }

    public void createTeams(GameMap map, PointManager pointManager, List<QueueTeam> teams) {
        if (teams != null) {
            for (int i = 0; i < teams.size(); i++) {
                QueueTeam queueTeam = teams.get(i);

                // Always 2 teams: index 0 -> first, index 1 -> last
                int pointIndex = (i == 0) ? 0 : pointManager.getPoints().size() - 1;

                GameTeam team = createTeam(
                        queueTeam.getColor(),
                        queueTeam.getName(),
                        map.getSpawns().get(i),
                        pointManager.getPoints().get(pointIndex)
                );

                for (Player player : queueTeam.getPlayers()) {
                    game.getPlayerManager().createProfile(player, team);
                }
            }
            return;
        }

        // Fallback: two default teams
        createTeam(NamedTextColor.RED, "Красные",
                map.getSpawns().getFirst(), pointManager.getPoints().getFirst());

        createTeam(NamedTextColor.BLUE, "Синие",
                map.getSpawns().get(1), pointManager.getPoints().getLast());
    }

    private GameTeam createTeam(NamedTextColor color, String name, Location spawn, Point firstPoint) {
        GameTeam team = new GameTeam(color, name, spawn);
        team.register(game.getUiManager().getScoreboard());
        team.setFirstPoint(firstPoint);
        teams.add(team);
        return team;
    }

    public void checkWinner() {
        List<GameTeam> alive = teams.stream()
                .filter(t -> t.getScore() > 0)
                .toList();

        if (alive.size() == 1) {
            game.announceWinner(alive.getFirst());
        } else if (alive.isEmpty()) {
            game.announceWinner(null);
        }
    }

    public List<GameTeam> getTeams() {
        return teams;
    }
}

