package me.harpervenom.hotspot.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.harpervenom.hotspot.statistics.Stats;
import me.harpervenom.hotspot.statistics.StatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class HotSpotExpansion extends PlaceholderExpansion {//

    private final StatsManager statsManager;

    public HotSpotExpansion(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors()); //
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "hotspot";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion(); //
    }

    @Override
    public boolean persist() {
        return true; //
    }

    private static final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {

        if (params.equalsIgnoreCase("player_info")) {

            Stats stats = statsManager.getStats(player.getUniqueId());

            Component level = stats.getLevelIcon();
            Component rank = stats.getRankIcon();
            Component skill = stats.getSkillIcon();

            Component combined = Component.empty()
//                    .append(level)
//                    .append(rank)
                    .append(skill);

            return mm.serialize(combined);
        }

        if (params.equalsIgnoreCase("player_name")) {
            return mm.serialize(statsManager.getName(player));
        }

        return null; //
    }
}
