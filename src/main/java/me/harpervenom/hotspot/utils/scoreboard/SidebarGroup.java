package me.harpervenom.hotspot.utils.scoreboard;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class SidebarGroup {

    private final String objectiveId;
    private final Component title;

    private final Set<Player> viewers = new HashSet<>();

    SidebarGroup(String objectiveId, Component title) {
        this.objectiveId = objectiveId;
        this.title = title;
        createObjective();
    }

    void updateViewers(Set<Player> newViewers) {
        // removed viewers
        for (Player p : viewers) {
            if (!newViewers.contains(p)) {
                removeFor(p);
            }
        }

        // added viewers
        for (Player p : newViewers) {
            if (viewers.add(p)) {
                showFor(p);
            }
        }

        viewers.retainAll(newViewers);
    }

    void send(List<Component> lines) {
        for (Player player : viewers) {
            sendLines(player, lines);
        }
    }

    void removeAll() {
        for (Player player : viewers) {
            removeFor(player);
        }
        viewers.clear();
    }

    /* ---- packets ---- */

    private void createObjective() {
        WrapperPlayServerScoreboardObjective create =
                new WrapperPlayServerScoreboardObjective(
                        objectiveId,
                        WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                        title,
                        WrapperPlayServerScoreboardObjective.RenderType.INTEGER
                );

        WrapperPlayServerDisplayScoreboard display =
                new WrapperPlayServerDisplayScoreboard(1, objectiveId);

        // no viewers yet — sent in showFor()
    }

    private void showFor(Player player) {
        WrapperPlayServerScoreboardObjective create =
                new WrapperPlayServerScoreboardObjective(
                        objectiveId,
                        WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                        title,
                        WrapperPlayServerScoreboardObjective.RenderType.INTEGER
                );

        WrapperPlayServerDisplayScoreboard display =
                new WrapperPlayServerDisplayScoreboard(1, objectiveId);

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, create);
        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, display);
    }

    private void removeFor(Player player) {
        WrapperPlayServerScoreboardObjective remove =
                new WrapperPlayServerScoreboardObjective(
                        objectiveId,
                        WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
                        title,
                        WrapperPlayServerScoreboardObjective.RenderType.INTEGER
                );

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, remove);
    }

    private void sendLines(Player player, List<Component> lines) {
        int score = lines.size();

        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);

            WrapperPlayServerUpdateScore packet =
                    new WrapperPlayServerUpdateScore(
                            "line_" + i,
                            WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                            objectiveId,
                            score - i,
                            line,
                            ScoreFormat.blankScore()
                    );

            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        }
    }
}


