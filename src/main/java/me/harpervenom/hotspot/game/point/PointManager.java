package me.harpervenom.hotspot.game.point;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.team.GameTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.harpervenom.hotspot.utils.Utils.playSound;
import static me.harpervenom.hotspot.utils.Utils.text;

public class PointManager {

    private enum CaptureResult {
        SUCCESS,
        ALREADY_OWNED,
        NOT_FIRST_AND_NO_NEIGHBOUR
    }

    private final Game game;
    private final List<Point> points;

    public PointManager(Game game) {
        this.game = game;
        points = game.getMap().getPoints();
    }

    public void setup() {
        for (Point point : points) {
            point.build();
        }
    }

    public Point getPoint(Block block) {
        return points.stream()
                .filter(m -> m.isBlock(block))
                .findFirst()
                .orElse(null);
    }

    public void capture(Point point, Player player) {
        if (point == null) return;

        GameTeam team = game.getPlayerManager().getTeam(player);
        if (team == null) return;

        CaptureResult result = checkCapture(point, team);

        switch (result) {
            case NOT_FIRST_AND_NO_NEIGHBOUR ->
                    player.sendActionBar(text("Вы не можете захватить эту точку", NamedTextColor.RED));

            case SUCCESS -> {
                if (point.getTeam() != null) {
                    playSound(Sound.BLOCK_BEACON_DEACTIVATE, 1, 2, point.getTeam().getPlayers());
                }
                point.setTeam(team);
                playSound(Sound.BLOCK_BEACON_ACTIVATE, 1, 2, team.getPlayers());
                game.getUiManager().update();
                updateDisplay();
            }
        }
    }

    private CaptureResult checkCapture(Point point, GameTeam team) {
        // Already owned by the team
        if (team.equals(point.getTeam())) {
            return CaptureResult.ALREADY_OWNED;
        }

        // First point: always capturable
        if (team.getFirstPoint().equals(point)) {
            return CaptureResult.SUCCESS;
        }

        // Check neighbor control
        boolean hasNeighbour = getNeighbours(point).stream()
                .anyMatch(neighbour -> team.equals(neighbour.getTeam()));

        return hasNeighbour ? CaptureResult.SUCCESS : CaptureResult.NOT_FIRST_AND_NO_NEIGHBOUR;
    }

    private List<Point> getNeighbours(Point point) {
        int index = points.indexOf(point);
        if (index == -1) return Collections.emptyList();

        List<Point> neighbours = new ArrayList<>();

        if (index > 0) {
            neighbours.add(points.get(index - 1));
        }
        if (index < points.size() - 1) {
            neighbours.add(points.get(index + 1));
        }

        return neighbours;
    }

    public Component buildPointsLine(GameTeam team, int maxPoints) {
        int points = getTeamPoints(team).size();
        Component line = Component.empty();

        // Filled squares
        if (points > 0) line = line.append(text("■".repeat(points), team.getColor()));

        // Empty squares
        int emptyCount = maxPoints - points;
        for (int i = 0; i < emptyCount; i++) {
            NamedTextColor color = (points == 0 && i == 0) ? team.getColor() : NamedTextColor.DARK_GRAY;
            line = line.append(text("□", color));
        }

        return line;
    }

    public void updateDisplay() {
        for (Point point : points) {
            List<Player> viewers = new ArrayList<>();

            for (GameTeam team : game.getTeamManager().getTeams()) {
                CaptureResult result = checkCapture(point, team);
//                if (getTeamPoints(team).size() > points.size() / 2) continue;
                if (result == CaptureResult.SUCCESS) {
                    viewers.addAll(team.getPlayers());
                }
            }
            point.setViewers(viewers);
        }
    }

    public void remove() {
        for (Point point : points) {
            point.remove();
        }
    }
    public List<Point> getPoints() {
        return points;
    }
    public List<Point> getTeamPoints(GameTeam team) {
        return points.stream().filter(point -> team.equals(point.getTeam())).toList();
    }
}
