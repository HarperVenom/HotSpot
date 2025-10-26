package me.harpervenom.hotspot.utils;

import org.bukkit.Bukkit;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class CountdownTimer {

    private final int startSeconds;
    private int countdownId = -1;
    private int timeLeft;
    private final Runnable onFinish;
    private final Runnable onTick;

    public CountdownTimer(int startSeconds, Runnable onFinish, Runnable onTick) {
        this.startSeconds = startSeconds;
        this.timeLeft = startSeconds;
        this.onFinish = onFinish;
        this.onTick = onTick;
    }

    public void start() {
        if (countdownId != -1) return; // already running

        countdownId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            timeLeft--;
            if (onTick != null) onTick.run();

            if (timeLeft <= 0) {
                cancel();
                if (onFinish != null) onFinish.run();
            }
        }, 20L, 20L);
    }

    public void skip() {
        timeLeft = 0;
        cancel();
        if (onFinish != null) onFinish.run();
    }

    public void reset() {
        cancel();
        timeLeft = startSeconds;
    }

    public void cancel() {
        if (countdownId != -1) {
            Bukkit.getScheduler().cancelTask(countdownId);
            countdownId = -1;
        }
    }

    public void setTimeLeft(int seconds) {
        timeLeft = seconds;
    }

    public int getTimeLeft() {
        return timeLeft;
    }
}

