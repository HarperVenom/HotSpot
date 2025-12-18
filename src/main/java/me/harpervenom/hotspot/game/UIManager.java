package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.point.PointManager;
import me.harpervenom.hotspot.game.team.GameTeam;
import me.harpervenom.hotspot.utils.scoreboard.CustomSidebar;
import me.harpervenom.hotspot.utils.scoreboard.PacketSidebar;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.harpervenom.hotspot.utils.Utils.formatTime;
import static me.harpervenom.hotspot.utils.Utils.text;

public class UIManager {
    private final Game game;
//    private final CustomSidebar scoreboard;
    private final PacketSidebar sidebar;

    // Three separate bars
    private final BossBar barBlue;
    private final BossBar barRed;
    private final BossBar barWhite;

    private final Set<Player> blueViewers = new HashSet<>();
    private final Set<Player> redViewers = new HashSet<>();
    private final Set<Player> whiteViewers = new HashSet<>();

    public UIManager(Game game) {
        this.game = game;
        sidebar = new PacketSidebar("game", text("Игра"));
//        scoreboard = new CustomSidebar("game", text("Игра"));
//        scoreboard.showHealth();
//        scoreboard.setPadding(1);

        barBlue = BossBar.bossBar(text(""), 1f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        barRed = BossBar.bossBar(text(""), 1f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        barWhite = BossBar.bossBar(text(""), 1f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
    }

    public void update() {
        sidebar.update(
                "spectators",
                new HashSet<>(game.getPlayerManager().getSpectators()),
                getLinesForTeam(null)
        );

        for (GameTeam team : game.getTeams()) {
            sidebar.update(
                    team.getId(),
                    new HashSet<>(team.getConnectedPlayers()),
                    getLinesForTeam(team)
            );
        }

//        List<Component> lines = new ArrayList<>();
//        lines.add(text(""));
//
//
//        for (GameTeam team : game.getTeams()) {
//            Component line = buildTeamLine(team, maxPoints);
//            lines.add(line);
//            lines.add(text(""));
//            sidebar.update(new HashSet<>(team.getConnectedPlayers()), lines);
//        }


//        sidebar.update();
//        scoreboard.updateLines(lines);

        updateBars();
    }

    private List<Component> getLinesForTeam(GameTeam team) {
        List<Component> lines = new ArrayList<>();
        lines.add(text(""));
        int maxPoints = game.getPointManager().getPoints().size();
        for (GameTeam currentTeam : game.getTeams()) {
            Component line = buildTeamLine(currentTeam, maxPoints, currentTeam.equals(team));
            lines.add(line);
        }
        lines.add(text(""));
        return lines;
    }

    // Updates all bars with the same content
    private void updateBars() {
        int vaultsTime = game.getVaultManager().getTime();

        Component info = text(formatTime(game.getElapsedTicks() / 20), NamedTextColor.WHITE)
                .append(text(" | ", NamedTextColor.GRAY))
                .append(text("Хранилища: "))
                .append(text(formatTime(vaultsTime)));

        barBlue.name(info);
        barRed.name(info);
        barWhite.name(info);
    }

    public void refreshBarViewers() {
        // remove bars from old viewers
        for (Player p : blueViewers)        p.hideBossBar(barBlue);
        for (Player p : redViewers)         p.hideBossBar(barRed);
        for (Player p : whiteViewers)       p.hideBossBar(barWhite);

        blueViewers.clear();
        redViewers.clear();
        whiteViewers.clear();

        // assign players again
        for (Player player : game.getPlayers()) {
            GameTeam team = game.getPlayerManager().getTeam(player);

            if (team == null) {
                whiteViewers.add(player);
                player.showBossBar(barWhite);
                continue;
            }

            NamedTextColor color = team.getColor();

            if (color == NamedTextColor.BLUE) {
                blueViewers.add(player);
                player.showBossBar(barBlue);
            } else if (color == NamedTextColor.RED) {
                redViewers.add(player);
                player.showBossBar(barRed);
            } else {
                whiteViewers.add(player);
                player.showBossBar(barWhite);
            }
        }
    }

    public void removeAllBars() {
        for (Player p : game.getPlayers()) {
            barBlue.removeViewer(p);
            barRed.removeViewer(p);
            barWhite.removeViewer(p);
        }
    }

    private Component buildTeamLine(GameTeam team, int maxPoints, boolean addMark) {
        PointManager pm = game.getPointManager();
        Component blocksLine = pm.buildPointsLine(team, maxPoints);

        int score = team.getScore();
        NamedTextColor scoreColor = score < 10 ? NamedTextColor.YELLOW : NamedTextColor.WHITE;

        int loss = Math.abs(game.getScoreManager().getScoreLoss(team));
        boolean hasArrow = loss != 0;

        // Format score as 3 digits with leading zeros
        String scoreStr = String.format("%03d", score);

        Component formattedScore = text("");

        if (score == 0) {
            // All zeros: first two gray, last one red
            formattedScore = formattedScore.append(text("00", NamedTextColor.GRAY))
                    .append(text("0", TextColor.color(204, 61, 61)));
        } else {
            // Score > 0: gray leading zeros, remaining digits in score color
            int firstNonZero = 0;
            while (firstNonZero < scoreStr.length() && scoreStr.charAt(firstNonZero) == '0') firstNonZero++;

            if (firstNonZero > 0) {
                formattedScore = formattedScore.append(
                        text(scoreStr.substring(0, firstNonZero), NamedTextColor.GRAY)
                );
            }

            formattedScore = formattedScore.append(
                    text(scoreStr.substring(firstNonZero), scoreColor)
            );
        }

        if (hasArrow) {
            formattedScore = formattedScore.append(
                    text(" ↓", lossToColor(loss))
            );
        }

        if (addMark) {
            formattedScore = formattedScore.append(text(" (Вы)", NamedTextColor.GRAY));
        }

        return Component.text()
                .append(text(" "))
                .append(blocksLine).append(Component.space())
                .append(formattedScore)
                .append(text(" "))
                .build();
    }

    private static TextColor lossToColor(int loss) {
        int maxLossForFullRed = 5;
        double t = Math.min(loss / (double) maxLossForFullRed, 1.0);
        if (loss == 1) return NamedTextColor.WHITE;

        int r = 255;
        int g = (int) (255 * (1.0 - t));
        int b = (int) (255 * (1.0 - t));

        return TextColor.color(r, g, b);
    }

//    public void setViewers(List<Player> viewers) {
//        scoreboard.setViewers(viewers);
//    }
//    public Scoreboard getSidebar() {
//        return scoreboard.getScoreboard();
//    }
//    public PacketSidebar getSideBar() {
//        return sidebar;
//    }

    public void clear() {
        sidebar.clearAll();
        removeAllBars();
    }
}
