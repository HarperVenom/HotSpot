package me.harpervenom.hotspot.utils;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

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
        spaceCount = 0;

        // Build the new entries (position -> paddedPlain)
        Map<Integer, String> newEntries = new HashMap<>();
        for (int i = 0; i < newLines.size(); i++) {
            String entry = buildOrUpdateEntry(i, newLines.get(i));
            newEntries.put(i, entry);
        }

        // Remove old entries that are not present anymore OR that changed value
        // (we must reset the old score value in the scoreboard)
        for (Iterator<Map.Entry<Integer, String>> it = lineEntries.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, String> old = it.next();
            int pos = old.getKey();
            String oldValue = old.getValue();

            String newValue = newEntries.get(pos);

            if (newValue == null) {
                // Position no longer exists -> remove old entry
                scoreboard.resetScores(oldValue);
                it.remove();
            } else if (!oldValue.equals(newValue)) {
                // Same position but different string -> reset old entry (to avoid duplicates)
                scoreboard.resetScores(oldValue);
                it.remove();
            } // otherwise it's identical and we keep it (no reset)
        }

        // Now replace tracking map with newEntries
        // (we already updated/created the actual Score objects in buildOrUpdateEntry)
        lineEntries.clear();
        lineEntries.putAll(newEntries);
    }

    private String buildOrUpdateEntry(int position, Component line) {
        String plain = LegacyComponentSerializer.legacySection().serialize(line);
        if (plain.isEmpty()) plain = space();

        String paddedPlain = " ".repeat(padding) + plain + " ".repeat(padding);
        Component paddedLine = Component.text(" ".repeat(padding))
                .append(line)
                .append(Component.text(" ".repeat(padding)));

        // Create/update the Score *without* resetting any old entries here

        Score score = objective.getScore(paddedPlain);
        score.customName(paddedLine);
        score.setScore(lines.size() - 1 - position);

        return paddedPlain;
    }

//    public void updateLines(List<Component> newLines) {
//        this.lines = newLines;
//
//        spaceCount = 0;
//
//        // Update each line individually
//        for (int i = 0; i < newLines.size(); i++) {
//            updateLine(i, newLines.get(i));
//        }
//
//        // Remove lines that are no longer present
//        for (int i = newLines.size(); i < lineEntries.size(); i++) {
//            String oldEntry = lineEntries.remove(i);
//            if (oldEntry != null) scoreboard.resetScores(oldEntry);
//        }
//    }
//
//    private void updateLine(int position, Component line) {
//        String plain = PlainTextComponentSerializer.plainText().serialize(line);
//
//        if (plain.isEmpty()) {
//            plain = space();
//        }
//
//        // Apply horizontal padding
//        String paddedPlain = " ".repeat(padding) + plain + " ".repeat(padding);
//        Component paddedLine = Component.text(" ".repeat(padding))
//                .append(line)
//                .append(Component.text(" ".repeat(padding)));
//
////        Bukkit.broadcastMessage(position + "");
//
//        String oldEntry = lineEntries.get(position);
//        if (oldEntry != null && oldEntry.equals(paddedPlain)) {
//            // Already same entry → skip
////            Bukkit.broadcastMessage("same");
//            return;
//        }
//
////        Bukkit.broadcastMessage("changed");
//
//        // Remove old entry if present
//        if (oldEntry != null) {
//            scoreboard.resetScores(oldEntry);
//        }
//
//        // Set new score
//        Score score = objective.getScore(paddedPlain);
//        score.customName(paddedLine);
//        score.setScore(lines.size() - 1 - position);
//
//        lineEntries.put(position, paddedPlain);
//    }

    private String space() {
        spaceCount++;
        return " ".repeat(spaceCount);
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}



