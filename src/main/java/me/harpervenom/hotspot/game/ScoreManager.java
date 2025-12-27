package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.team.GameTeam;
import org.bukkit.Sound;

import java.util.List;

import static me.harpervenom.hotspot.utils.Utils.playSound;

public class ScoreManager {

    private final Game game;
//    private final static int protectionSeconds = 15;
    private boolean protectionActive = true; // Flag to indicate if protection is on

    public ScoreManager(Game game) {
        this.game = game;
    }

    public void updateScores() {
        int seconds = game.getElapsedTicks() / 20;

        // Update protection flag
//        if (seconds >= protectionSeconds && protectionActive) {
//            protectionActive = false; // Protection just ended
//            sendMessage(text("Защита снята! Очки начинают сниматься!"), game.getPlayers());
//        }

        for (GameTeam team : game.getTeams()) {
            int loss = getScoreLoss(team);
            if (loss < 0) {
                team.setScore(team.getScore() + loss);
                if (team.getScore() < 10) {
                    playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 1, 0.5f, team.getConnectedPlayers());
                }
            }
        }
    }

    public int getScoreLoss(GameTeam team) {
        List<GameTeam> teams = game.getTeams();
        GameTeam t1 = teams.get(0);
        GameTeam t2 = teams.get(1);

        int p1 = game.getPointManager().getTeamPoints(t1).size();
        int p2 = game.getPointManager().getTeamPoints(t2).size();

        int diff;
        if (team.equals(t1)) {
            diff = p2 - p1;
        } else {
            diff = p1 - p2;
        }

        if (diff <= 0) return 0;

        int loss = (diff + 1) / 2; // ceil(diff / 2)
        return -loss;
    }

    public boolean isProtectionActive() {
        return protectionActive;
    }
}

