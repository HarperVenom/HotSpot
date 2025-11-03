package me.harpervenom.hotspot.game.team;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.game.point.Point;
import me.harpervenom.hotspot.game.point.PointManager;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

public class GameTeamManager {

    private final Game game;
    private final List<GameTeam> teams = new ArrayList<>();

    public GameTeamManager(Game game) {
        this.game = game;
    }

    public void createTeams(GameMap map, PointManager pointManager) {
        createTeam(NamedTextColor.RED, "Красные", map.getBases().getFirst(), pointManager.getPoints().getFirst());
        createTeam(NamedTextColor.BLUE, "Синие", map.getBases().get(1), pointManager.getPoints().getLast());
    }

    private void createTeam(NamedTextColor color, String name, TeamBase base, Point firstPoint) {
        GameTeam gameTeam = new GameTeam(color, name, base);
        gameTeam.register(game.getScoreboardManager().getScoreboard());
        gameTeam.setFirstPoint(firstPoint);
        teams.add(gameTeam);
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

