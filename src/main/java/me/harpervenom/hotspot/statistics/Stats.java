package me.harpervenom.hotspot.statistics;

import me.harpervenom.hotspot.game.profile.GameStats;

public class Stats {

    private int exp;
    private double rank;
    private int gamesPlayed;
    private int gamesWon;
    private int kills;
    private int deaths;
    private double dealtDamage;
    private double takenDamage;
    private double preventedDamage;
    private int capturedPoints;

    public Stats(
            int exp,
            double rank,
            int gamesPlayed,
            int gamesWon,
            int kills,
            int deaths,
            double dealtDamage,
            double takenDamage,
            double preventedDamage,
            int capturedPoints
    ) {
        this.exp = exp;
        this.rank = rank;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.kills = kills;
        this.deaths = deaths;
        this.dealtDamage = dealtDamage;
        this.takenDamage = takenDamage;
        this.preventedDamage = preventedDamage;
        this.capturedPoints = capturedPoints;
    }

    public void addGameStats(GameStats stats) {
        this.exp += stats.getExp();
        this.rank += stats.getRankChange();
        this.gamesPlayed++;
        this.gamesWon += stats.isWinner() ? 1 : 0;
        this.kills += stats.getKills();
        this.deaths += stats.getDeaths();
        this.dealtDamage += stats.getDealtDamage();
        this.takenDamage += stats.getTakenDamage();
        this.preventedDamage += stats.getPreventedDamage();
        this.capturedPoints += stats.getCapturedPoints();
    }

    public int getExp() {
        return exp;
    }

    public double getRank() {
        return rank;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public double getDealtDamage() {
        return dealtDamage;
    }

    public double getTakenDamage() {
        return takenDamage;
    }

    public double getPreventedDamage() {
        return preventedDamage;
    }

    public int getCapturedPoints() {
        return capturedPoints;
    }
}

