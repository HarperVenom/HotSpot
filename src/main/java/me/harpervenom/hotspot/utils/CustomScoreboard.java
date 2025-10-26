package me.harpervenom.hotspot.utils;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.harpervenom.hotspot.utils.Utils.text;

public class CustomScoreboard {

    private final Scoreboard scoreboard;
    private final Objective objective;

    private List<Player> viewers = new ArrayList<>();
    private List<Component> lines = new ArrayList<>();

    private final Map<Integer, String> lineEntries = new HashMap<>(); // slot → entry
    private int spaceCount = 0; // for unique empty lines
    private int padding = 0;    // horizontal padding

    public CustomScoreboard(String name, Component title) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        this.scoreboard = manager.getNewScoreboard();

        this.objective = scoreboard.registerNewObjective(name, Criteria.DUMMY, title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.numberFormat(NumberFormat.blank());
    }

    public void showHealth() {
        Objective healthObjective = scoreboard.registerNewObjective("playersHealth", Criteria.HEALTH, text("❤", NamedTextColor.RED));
        healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        healthObjective.setRenderType(RenderType.HEARTS);
    }

    public Objective getHealthObjective() {
        return scoreboard.getObjective("playersHealth");
    }

    public void setPadding(int padding) {
        this.padding = Math.max(0, padding);
    }

    public void setViewers(List<Player> players) {
        // Remove old viewers
        for (Player old : viewers) {
            if (!players.contains(old) && old.getScoreboard().equals(scoreboard)) {
                old.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }

        // Add new viewers
        for (Player added : players) {
            if (!viewers.contains(added)) {
                added.setScoreboard(scoreboard);
            }
        }

        this.viewers = new ArrayList<>(players);
    }

    public void updateLines(List<Component> newLines) {
        this.lines = newLines;

        // Update each line individually
        for (int i = 0; i < newLines.size(); i++) {
            updateLine(i, newLines.get(i));
        }

        // Remove lines that are no longer present
        for (int i = newLines.size(); i < lineEntries.size(); i++) {
            String oldEntry = lineEntries.remove(i);
            if (oldEntry != null) scoreboard.resetScores(oldEntry);
        }
    }

    private void updateLine(int position, Component line) {
        String plain = PlainTextComponentSerializer.plainText().serialize(line);

        if (plain.isEmpty()) {
            plain = space();
        }

        // Apply horizontal padding
        String paddedPlain = " ".repeat(padding) + plain + " ".repeat(padding);
        Component paddedLine = Component.text(" ".repeat(padding))
                .append(line)
                .append(Component.text(" ".repeat(padding)));

        String oldEntry = lineEntries.get(position);
        if (oldEntry != null && oldEntry.equals(paddedPlain)) {
            // Already same entry → skip
            return;
        }

        // Remove old entry if present
        if (oldEntry != null) {
            scoreboard.resetScores(oldEntry);
        }

        // Set new score
        Score score = objective.getScore(paddedPlain);
        score.customName(paddedLine);
        score.setScore(lines.size() - 1 - position);

        lineEntries.put(position, paddedPlain);
    }

    private String space() {
        spaceCount++;
        return " ".repeat(spaceCount);
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}



