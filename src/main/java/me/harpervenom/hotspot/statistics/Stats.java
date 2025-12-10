package me.harpervenom.hotspot.statistics;

import me.harpervenom.hotspot.game.profile.GameStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;

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

    public int getRankProgress() {
        return (int) ((rank - Math.floor(rank)) * 100);
    }

    public static Component rankSymbol(double rank) {
        int r = Math.max(0, (int) Math.floor(rank));

        TextColor color = switch (r) {
            case 0 -> TextColor.color(184, 155, 104);
            case 1 -> TextColor.color(150, 150, 150);
            case 2 -> TextColor.color(217, 129, 94);
            case 3 -> TextColor.color(230, 250, 230);
            case 4 -> TextColor.color(255, 215, 0);
            case 5 -> TextColor.color(0, 255, 233);
            case 6 -> TextColor.color(74, 218, 86);
            default -> TextColor.color(213, 113, 255);
        };

        return Component.text("[" + r + "]", color);
    }

    public static Component levelSymbolFromExp(int exp) {
        return levelSymbol(getLevelFromPoints(exp));
    }

    public static Component levelSymbol(int level) {
        return Component.text("[" + level + "]", TextColor.color(217, 255, 217));
    }

    public static String getProgressString(int points) {
        int level = getLevelFromPoints(points);
        return (points - totalPointsRequiredForLevel(level)) + "/" + pointsRequiredForLevel(level + 1);
    }

    public static int getLevelFromPoints(int points) {
        int level = 0;
        int cost = 50;

        while (points >= cost) {
            points -= cost;
            level++;
            cost += 10;
        }

        return level;
    }

    public static int pointsRequiredForLevel(int level) {
        if (level <= 0) return 0;
        return 50 + (level - 1) * 10;
    }

    public static int totalPointsRequiredForLevel(int level) {
        if (level <= 0) return 0;
        return level * (100 + (level - 1) * 10) / 2;
    }
}

