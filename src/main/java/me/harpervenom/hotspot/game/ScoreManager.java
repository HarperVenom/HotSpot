package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.team.GameTeam;

import java.util.List;

import static me.harpervenom.hotspot.utils.Utils.sendMessage;
import static me.harpervenom.hotspot.utils.Utils.text;

public class ScoreManager {

    private final Game game;
    private final static int protectionSeconds = 15;
    private boolean protectionActive = true; // Flag to indicate if protection is on

    public ScoreManager(Game game) {
        this.game = game;
    }

    public void updateScores() {
        int seconds = game.getElapsedTicks() / 20;

        // Update protection flag
        if (seconds >= protectionSeconds && protectionActive) {
            protectionActive = false; // Protection just ended
            sendMessage(text("Защита снята! Очки начинают сниматься!"), game.getPlayers());
        }

        for (GameTeam team : game.getTeams()) {
            int loss = getScoreLoss(team);
            if (loss < 0) {
                team.setScore(team.getScore() + loss);
            }
        }
    }

    public int getScoreLoss(GameTeam team) {
        // Return 0 if protection is active
        if (protectionActive) return 0;

        List<GameTeam> teams = game.getTeams();
        GameTeam t1 = teams.get(0);
        GameTeam t2 = teams.get(1);

        int p1 = game.getPointManager().getTeamPoints(t1).size();
        int p2 = game.getPointManager().getTeamPoints(t2).size();

        if (p1 == p2) {
            return -1; // Both lose 1
        }

        if (team.equals(t1)) {
            return Math.min(0, p1 - p2); // Negative if losing, 0 if leading
        } else {
            return Math.min(0, p2 - p1);
        }
    }

    public boolean isProtectionActive() {
        return protectionActive;
    }
}

