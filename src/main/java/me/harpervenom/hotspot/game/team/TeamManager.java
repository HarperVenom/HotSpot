package me.harpervenom.hotspot.game.team;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.game.point.Point;
import me.harpervenom.hotspot.game.point.PointManager;
import me.harpervenom.hotspot.game.trader.Trader;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TeamManager {

    private final Game game;
    private final List<GameTeam> teams = new ArrayList<>();

    public TeamManager(Game game) {
        this.game = game;
    }

    public void createTeams(GameMap map, PointManager pointManager) {
        createTeam(NamedTextColor.RED, "Красные", map.getSpawns().getFirst(), pointManager.getPoints().getFirst());
        createTeam(NamedTextColor.BLUE, "Синие", map.getSpawns().get(1), pointManager.getPoints().getLast());
    }

    private void createTeam(NamedTextColor color, String name, Location spawn, Point firstPoint) {
        GameTeam gameTeam = new GameTeam(color, name, spawn);
        gameTeam.register(game.getUiManager().getScoreboard());
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

