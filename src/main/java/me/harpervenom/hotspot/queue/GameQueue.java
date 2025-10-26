package me.harpervenom.hotspot.queue;

import me.harpervenom.hotspot.game.GameModeEnum;
import me.harpervenom.hotspot.game.GameSettings;
import me.harpervenom.hotspot.player.GamePlayer;
import me.harpervenom.hotspot.utils.CountdownTimer;
import me.harpervenom.hotspot.utils.CustomScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.harpervenom.hotspot.utils.Utils.formatTime;
import static me.harpervenom.hotspot.utils.Utils.text;

public class GameQueue {

    private final QueueManager queueManager;
    private final GameModeEnum gameMode;
    private final List<Player> players = new ArrayList<>();
    private final CustomScoreboard scoreboard;
    private final GameSettings gameSettings;
    private final CountdownTimer timer;
    private boolean isReady;

    private final List<Player> skippingPlayers = new ArrayList<>();

    public GameQueue(QueueManager queueManager, GameModeEnum mode) {
        this.queueManager = queueManager;
        scoreboard = new CustomScoreboard("queue", text(" Очередь ").append(mode.getName().append(text(" "))));
        scoreboard.setPadding(1);
        gameMode = mode;
        gameSettings = mode.getGameSettings();
        timer = new CountdownTimer(60,
                () -> {
                    ready();
                },
                () -> {
                    updateScoreboard();
                }
        );
    }

    private void ready() {
        isReady = true;
        queueManager.readyQueue(this);
        updateScoreboard();
    }

    public void addPlayer(Player p) {
        players.add(p);

        scoreboard.setViewers(players);

        if (players.size() == 1) {
            // First player → start 2 min timer
            timer.setTimeLeft(120);
            timer.start();
        } else if (players.size() == 2) {
            // More than one player → check if still above 60s, cut to 1 min
            if (timer.getTimeLeft() > 60) {
                timer.setTimeLeft(60);
            }
        }

        updateScoreboard();
    }

    public void removePlayer(Player player) {
        players.remove(player);

        skippingPlayers.remove(player);

        scoreboard.setViewers(players);
        updateScoreboard();

        if (players.isEmpty()) {
            timer.reset();
        }
    }

    public void toggleSkip(Player player) {
        if (!skippingPlayers.remove(player)) skippingPlayers.add(player);

        checkSkips();
    }

    public boolean isSkipping(Player player) {
        return skippingPlayers.contains(player);
    }

    public void checkSkips() {
        int numberSkipping = skippingPlayers.size();

        int totalPlayers = getPlayers().size();
        int numberPlayersNeeded = Math.max(2, (int) Math.ceil(totalPlayers * 0.8));

        actionBarMessage(text("Пропуск ожидания " + numberSkipping + "/" + numberPlayersNeeded, NamedTextColor.YELLOW));

        boolean canSkip = totalPlayers > 1 && numberSkipping >= numberPlayersNeeded;

        // for tests
        canSkip = false;

        if (canSkip) {
            timer.skip();
        }
    }

    public void actionBarMessage(Component message) {
        for (Player player : getPlayers()) {
            player.sendActionBar(message);
        }
    }

    public void playSound(Sound sound, float volume, float pitch) {
        for (Player player : players) {
            player.playSound(player, sound, volume, pitch);
        }
    }

//    public void dispose() {
//        List<Player> playersCopy = List.copyOf(players);
//        playersCopy.forEach(queueManager::removePlayerFromQueue);
//    }

    public boolean isReady() {
        return isReady;
    }

    public int getMaxPlayers() {
        return gameSettings.getMaxPlayers();
    }
    public boolean isFull() {
        return players.size() >= gameSettings.getMaxPlayers();
    }

//    public GamePlayer getGamePlayer(Player player) {
//        return players.stream().filter(gamePlayer -> gamePlayer.getPlayer().equals(player)).toList().getFirst();
//    }

    public List<Player> getPlayers() {
        return players;
    }

    private void updateScoreboard() {
        scoreboard.updateLines(List.of(
                text(""),
                text("Игроки: " + players.size() + "/" + gameSettings.getMaxPlayers(), NamedTextColor.YELLOW),
                text(""),
                isReady ? text("Подготовка...") : text("До начала: " + formatTime(timer.getTimeLeft())),
                text("")
        ));
    }

    public GameSettings getSettings() {
        return gameSettings;
    }
    public GameModeEnum getGameMode() {
        return gameMode;
    }
}
