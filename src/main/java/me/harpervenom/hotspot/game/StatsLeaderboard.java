package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.profile.GameProfile;
import me.harpervenom.hotspot.game.profile.GameStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.ToDoubleFunction;

import static net.kyori.adventure.text.Component.text;

public final class StatsLeaderboard {

    private StatsLeaderboard() {}

    /* =========================
       Data containers
       ========================= */

    public record StatEntry(GameProfile profile, double value) {}

    public record Leaderboards(
            List<StatEntry> dealtDamage,
            List<StatEntry> takenDamage,
            List<StatEntry> preventedDamage,
            List<StatEntry> kills,
            List<StatEntry> deaths,
            List<StatEntry> capturedPoints
    ) {}

    /* =========================
       Build leaderboards ONCE
       ========================= */

    public static Leaderboards buildLeaderboards(List<GameProfile> profiles) {
        return new Leaderboards(
                sort(profiles, GameStats::getDealtDamage),
                sort(profiles, GameStats::getTakenDamage),
                sort(profiles, GameStats::getPreventedDamage),
                sort(profiles, GameStats::getKills),
                sort(profiles, GameStats::getDeaths),
                sort(profiles, GameStats::getCapturedPoints)
        );
    }

    private static List<StatEntry> sort(
            List<GameProfile> profiles,
            ToDoubleFunction<GameStats> extractor
    ) {
        return profiles.stream()
                .map(p -> new StatEntry(p, extractor.applyAsDouble(p.getStats())))
                .sorted(Comparator.comparingDouble(StatEntry::value).reversed())
                .toList();
    }

    /* =========================
       Message building (per viewer)
       ========================= */

    public static Component buildPersonalStatsMessage(
            Leaderboards lb,
            GameProfile viewer
    ) {
        return Component.empty()
                .append(renderStat("Нанесенный Урон", lb.dealtDamage(), viewer, ""))
//                .append(Component.newline())
                .append(renderStat("Полученный Урон", lb.takenDamage(), viewer, ""))
//                .append(Component.newline())
                .append(renderStat("Предотвращенный Урон", lb.preventedDamage(), viewer, ""))
//                .append(Component.newline())
                .append(renderStat("Убийства", lb.kills(), viewer, ""))
//                .append(Component.newline())
                .append(renderStat("Смерти", lb.deaths(), viewer, ""))
//                .append(Component.newline())
                .append(renderStat("Захваченные Точки", lb.capturedPoints(), viewer, ""));
    }

    private static Component renderStat(
            String title,
            List<StatEntry> sorted,
            @Nullable GameProfile viewer,
            String suffix
    ) {
        Component comp = text(title + ":\n", NamedTextColor.AQUA);

        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            StatEntry e = sorted.get(i);
            comp = comp.append(
                    text("#" + (i + 1) + " ", NamedTextColor.YELLOW)
                            .append(e.profile().getName().color(e.profile.getTeam().getColor()))
                            .append(text(" - " + (int) e.value() + suffix + "\n", NamedTextColor.WHITE))
            );
        }

        // no viewer → no personal placement
        if (viewer == null) {
            return comp;
        }

        int pos = getPosition(sorted, viewer);
        if (pos > 3) {
            StatEntry self = sorted.get(pos - 1);
            comp = comp.append(
                    text("§7#" + pos + " You §7- " + (int) self.value() + suffix + "\n")
            );
        }

        return comp;
    }

    private static int getPosition(List<StatEntry> sorted, GameProfile profile) {
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).profile().equals(profile)) {
                return i + 1;
            }
        }
        return -1;
    }
}
