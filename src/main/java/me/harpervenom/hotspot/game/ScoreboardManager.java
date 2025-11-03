package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.point.PointManager;
import me.harpervenom.hotspot.game.team.GameTeam;
import me.harpervenom.hotspot.utils.CustomScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.harpervenom.hotspot.utils.Utils.formatTime;
import static me.harpervenom.hotspot.utils.Utils.text;

public class ScoreboardManager {
    private final Game game;
    private final CustomScoreboard scoreboard;

    public ScoreboardManager(Game game) {
        this.game = game;
        scoreboard = new CustomScoreboard("game", text("Игра"));
        scoreboard.showHealth();
        scoreboard.setPadding(1);
    }

    public void update() {
        List<Component> lines = new ArrayList<>();
        lines.add(text(""));
//        lines.add(text(formatTime(game.getElapsedTicks() / 20)));
//        lines.add(text(""));

        int maxPoints = game.getPointManager().getPoints().size();
        for (GameTeam team : game.getTeams()) {
            Component line = buildTeamLine(team, maxPoints);
            lines.add(line);
        }
        lines.add(text(""));

        scoreboard.updateLines(lines);
    }

    private Component buildTeamLine(GameTeam team, int maxPoints) {
        PointManager pm = game.getPointManager();
        Component blocksLine = pm.buildPointsLine(team, maxPoints);

        int score = team.getScore();
        NamedTextColor scoreColor = score < 10 ? NamedTextColor.YELLOW : NamedTextColor.WHITE;
        boolean hasArrow = game.getScoreManager().getScoreLoss(team) != 0;

        // Format score as 3 digits with leading zeros
        String scoreStr = String.format("%03d", score);

        Component formattedScore = text("");

        if (score == 0) {
            // All zeros: first two gray, last one red
            formattedScore = formattedScore.append(text("00", NamedTextColor.DARK_GRAY))
                    .append(text("0", TextColor.color(204, 61, 61)));
        } else {
            // Score > 0: gray leading zeros, remaining digits in score color
            int firstNonZero = 0;
            while (firstNonZero < scoreStr.length() && scoreStr.charAt(firstNonZero) == '0') firstNonZero++;

            if (firstNonZero > 0) {
                formattedScore = formattedScore.append(
                        text(scoreStr.substring(0, firstNonZero), NamedTextColor.DARK_GRAY)
                );
            }

            formattedScore = formattedScore.append(
                    text(scoreStr.substring(firstNonZero), scoreColor)
            );
        }

        return Component.text().append(blocksLine).append(Component.space())
                .append(formattedScore)
                .append(text(hasArrow ? " ↓" : ""))
                .build();
    }

    public void setViewers(List<Player> viewers) {
        scoreboard.setViewers(viewers);
    }
    public Scoreboard getScoreboard() {
        return scoreboard.getScoreboard();
    }
}
