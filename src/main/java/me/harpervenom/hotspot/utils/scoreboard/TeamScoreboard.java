package me.harpervenom.hotspot.utils.scoreboard;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

import static me.harpervenom.hotspot.utils.Utils.text;

public final class TeamScoreboard {

    private final Scoreboard scoreboard;
    private final Map<String, Team> teams = new HashMap<>();

    public TeamScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public Team getOrCreateTeam(String id, NamedTextColor color) {
        return teams.computeIfAbsent(id, k -> {
            Team team = scoreboard.registerNewTeam(k);
            team.color(color);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            return team;
        });
    }

    public void addPlayer(Player player, String teamId) {
        Team team = teams.get(teamId);
        if (team != null) {
            team.addEntity(player);
            player.setScoreboard(scoreboard);
        }
    }

    public void removePlayer(Player player) {
        teams.values().forEach(t -> t.removeEntry(player.getName()));
        if (player.getScoreboard().equals(scoreboard)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void showHealth() {
        Objective healthObjective = scoreboard.registerNewObjective("playersHealth", Criteria.HEALTH, text("❤", NamedTextColor.RED));
        healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        healthObjective.setRenderType(RenderType.HEARTS);
    }
}

