package me.harpervenom.hotspot.game.team;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.ScoreManager;
import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.game.point.Point;
import me.harpervenom.hotspot.game.point.PointManager;
import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import me.harpervenom.hotspot.utils.scoreboard.TeamScoreboard;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class GameTeamManager {

    private final Game game;
    private final List<GameTeam> teams = new ArrayList<>();
    private final TeamScoreboard scoreboard;

    public GameTeamManager(Game game) {
        this.game = game;
        scoreboard = new TeamScoreboard();
        scoreboard.showHealth();
    }

    public void createTeams(GameMap map, PointManager pointManager, List<QueueTeam> queueTeams) {
        if (queueTeams != null) {
            for (int i = 0; i < queueTeams.size(); i++) {
                QueueTeam queueTeam = queueTeams.get(i);

                int pointIndex = (i == 0)
                        ? 0
                        : pointManager.getPoints().size() - 1;

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

        // fallback teams
        createDefaultTeams(map, pointManager);
    }

    private void createDefaultTeams(GameMap map, PointManager pointManager) {
        GameTeam red = createTeam(
                NamedTextColor.RED,
                "Красные",
                map.getSpawns().getFirst(),
                pointManager.getPoints().getFirst()
        );

        GameTeam blue = createTeam(
                NamedTextColor.BLUE,
                "Синие",
                map.getSpawns().get(1),
                pointManager.getPoints().getLast()
        );
    }

    private GameTeam createTeam(
            NamedTextColor color,
            String name,
            Location spawn,
            Point firstPoint
    ) {
        GameTeam gameTeam = new GameTeam(color, name, spawn);
        gameTeam.setFirstPoint(firstPoint);

        teams.add(gameTeam);

        // 🔑 Register Bukkit team (INVISIBLE scoreboard)
        Team team = scoreboard.getOrCreateTeam(gameTeam.getId(), color);
        gameTeam.setTeam(team);

        return gameTeam;
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

    public TeamScoreboard getScoreboard() {
        return scoreboard;
    }
}

