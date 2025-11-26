package me.harpervenom.hotspot.utils;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import static me.harpervenom.hotspot.HotSpot.plugin;

import java.util.function.IntConsumer;

public class CountdownTimer {

    private final int startSeconds;
    private int countdownId = -1;
    private int timeLeft; // seconds
    private final Runnable onFinish;
    private final IntConsumer onTick;

    public CountdownTimer(int startSeconds, Runnable onFinish, IntConsumer onTick) {
        this.startSeconds = startSeconds;
        this.timeLeft = this.startSeconds;
        this.onFinish = onFinish;
        this.onTick = onTick;
    }

    public void start() {
        if (countdownId != -1) {
            cancel();
        }

        countdownId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            timeLeft--;

            if (onTick != null) {
                onTick.accept(timeLeft);
            }

            if (timeLeft <= 0) {
                cancel();
                if (onFinish != null) onFinish.run();
            }
        }, 0, 20L);
    }

    public void skip() {
        skip(0);
    }

    public void skip(int secondsLeft) {
        cancel();                      // hard stop old task
        timeLeft = secondsLeft + 1;

        if (timeLeft <= 0) {
            if (onFinish != null) onFinish.run();
            return;
        }

        start();                       // restart task cleanly
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
    public boolean isRunning() {
        return countdownId != -1;
    }
}

