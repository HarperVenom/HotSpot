package me.harpervenom.hotspot.game.profile;

public class GameStats {

    private boolean isWinner = false;
    private int exp = 0;
    private double rankChange = 0;

    private double dealtDamage = 0;
    private double takenDamage = 0;
    private double preventedDamage = 0;
    private int kills = 0;
    private int capturedPoints = 0;
    private int deaths = 0;

    private double lastDealtDamage = 0;
    private int lastKills = 0;
    private int lastCapturedPoints = 0;

    public void finalizeMatch(double currentRank, boolean isWinner, boolean isRanked) {
        this.isWinner = isWinner;

        double performance =
                dealtDamage * 0.001 +
                        preventedDamage * 0.0012 +
                        kills * 0.1 +
                        capturedPoints * 0.03 -
                        deaths * 0.05;

        performance = Math.max(0.0, performance);

        exp += 5 + (int) Math.min(5.0, performance);
        if (isWinner) exp += 15;

        if (!isRanked) return;
        double baseDelta = 0.3;
        double rankFloor = Math.floor(currentRank);
        double scaling = 1.0 / Math.pow(1.0 + rankFloor, 0.7);
        double lossMultiplier = 1.0 + (rankFloor * 0.15);

        double delta = isWinner
                ? baseDelta * scaling * (1.0 + performance * 0.02)
                : -baseDelta * scaling * lossMultiplier;

        rankChange = clamp(delta, -0.3, 0.3);
        if (currentRank == 0) rankChange = 0;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public void addDealtDamage(double damage) {
        dealtDamage += damage;
        lastDealtDamage += damage;
    }
    public void addTakenDamage(double damage) {
        takenDamage += damage;
    }
    public void addPreventedDamage(double damage) {
        preventedDamage += damage;
    }
    public void addDeath() {
        deaths++;
    }
    public void addKill() {
        kills++;
        lastKills++;
    }
    public void addCapture() {
        capturedPoints++;
        lastCapturedPoints++;
    }

    public boolean isWinner() {return isWinner;}
    public int getExp() {return exp;}
    public double getRankChange() {return rankChange;}
    public double getDealtDamage() {
        return dealtDamage;
    }
    public double getTakenDamage() {
        return takenDamage;
    }
    public double getPreventedDamage() {
        return preventedDamage;
    }
    public int getKills() {
        return kills;
    }
    public int getCapturedPoints() {
        return capturedPoints;
    }
    public int getDeaths() {
        return deaths;
    }

    public void resetBounty() {
        lastDealtDamage = 0;
        lastKills = 0;
        lastCapturedPoints = 0;
    }

    public int getBounty() {
        return Math.min(100, (int) (lastDealtDamage * 0.01
                + lastKills * 5
                + lastCapturedPoints * 0.5));
    }
}
