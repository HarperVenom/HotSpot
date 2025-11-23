package me.harpervenom.hotspot.queue;

import me.harpervenom.hotspot.game.GameModeEnum;
import me.harpervenom.hotspot.game.GameSettings;
import me.harpervenom.hotspot.queue.players.QueuePlayerOrganizer;
import me.harpervenom.hotspot.queue.players.SimpleQueueOrganizer;
import me.harpervenom.hotspot.queue.players.TeamQueueOrganizer;
import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import me.harpervenom.hotspot.utils.CountdownTimer;
import me.harpervenom.hotspot.utils.CustomScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

import static me.harpervenom.hotspot.utils.Utils.formatTime;
import static me.harpervenom.hotspot.utils.Utils.text;

public class GameQueue {

    private final QueueManager queueManager;
    private final GameModeEnum gameMode;
    private final QueuePlayerOrganizer organizer;
    private final CustomScoreboard scoreboard;
    private final GameSettings settings;
    private final CountdownTimer timer;
    private boolean isReady;

    private final List<Player> viewers = new ArrayList<>();

    private final List<Player> skippingPlayers = new ArrayList<>();

    private final Player owner;

    public GameQueue(QueueManager queueManager, GameModeEnum mode) {
        this(queueManager, mode, null);
    }

    public GameQueue(QueueManager queueManager, GameModeEnum mode, Player owner) {
        this.queueManager = queueManager;
        scoreboard = new CustomScoreboard("queue", text(" Очередь "));
        scoreboard.setPadding(1);
        gameMode = mode;
        settings = mode.getSettings();
        timer = new CountdownTimer(120,
                () -> {
                    ready();
                },
                () -> {
                    updateScoreboard();
                }
        );

        this.owner = owner;

        if (mode.getSettings().isCustom()) {
            organizer = new TeamQueueOrganizer(this);
        } else {
            organizer = new SimpleQueueOrganizer();
        }

        updateScoreboard();
    }

    private void ready() {
        isReady = true;
        queueManager.readyQueue(this);
        updateScoreboard();
    }

    public void addPlayer(Player player, QueueTeam team) {
        if (team != null) {
            organizer.addPlayerToTeam(player, team);
        } else {
            organizer.addPlayer(player);
        }

        List<Player> players = organizer.getAllPlayers();

        if (players.size() == 1) {
            timer.start();
        } else if (players.size() >= 2) {
            if (timer.getTimeLeft() > 60) {
                timer.setTimeLeft(60);
            }
        }

        updateScoreboard();
    }

    public void removePlayer(Player player, boolean silent) {
        organizer.removePlayer(player);

        skippingPlayers.remove(player);

        List<Player> players = organizer.getAllPlayers();

        if (!silent && players.isEmpty()) {
            timer.reset();
        }

        updateScoreboard();
    }

    public void toggleSkip(Player player) {
        if (!skippingPlayers.remove(player)) skippingPlayers.add(player);
    }

    public boolean isSkipping(Player player) {
        return skippingPlayers.contains(player);
    }

    public void checkSkips() {
        boolean canSkip = true;

        if (settings.isCustom()) {
            canSkip = skippingPlayers.contains(owner);
        } else {
            int numberSkipping = skippingPlayers.size();

            int totalPlayers = getPlayers().size();
            int numberPlayersNeeded = Math.max(2, (int) Math.ceil(totalPlayers * 0.8));

            actionBarMessage(text("Пропуск ожидания " + numberSkipping + "/" + numberPlayersNeeded, NamedTextColor.YELLOW));

            canSkip = totalPlayers > 1 && numberSkipping >= numberPlayersNeeded;

            // tests
            canSkip = true;
        }

        if (canSkip) {
            timer.skip();
        }
    }

    public QueueTeam getTeam(Player player) {
        if (organizer instanceof TeamQueueOrganizer) {
            return ((TeamQueueOrganizer) organizer).getTeamManager().getTeam(player);
        }
        return null;
    }

    public void actionBarMessage(Component message) {
        for (Player player : getPlayers()) {
            player.sendActionBar(message);
        }
    }

    public void playSound(Sound sound, float volume, float pitch) {
        for (Player player : organizer.getAllPlayers()) {
            player.playSound(player, sound, volume, pitch);
        }
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isOwner(Player player) {
        return player.equals(owner);
    }

    public int getMaxPlayers() {
        return settings.getMaxPlayers();
    }
//    public boolean isFull() {
//        return players.size() >= gameSettings.getMaxPlayers();
//    }

    public void addViewer(Player player) {
        viewers.add(player);
        scoreboard.setViewers(viewers);
    }

    public void removeViewer(Player player) {
        viewers.remove(player);
        scoreboard.setViewers(viewers);
    }

    public List<Player> getPlayers() {
        return organizer.getAllPlayers();
    }

    private void updateScoreboard() {
        scoreboard.updateLines(List.of(
                text(""),
                text("Игроки: " + organizer.getAllPlayers().size() + "/" + settings.getMaxPlayers(), NamedTextColor.YELLOW),
                text(""),
                isReady ? text("Запуск...") : text("До начала: " + formatTime(timer.getTimeLeft())),
                text("")
        ));
    }

    public void clean() {
       scoreboard.setViewers(new ArrayList<>());
       timer.cancel();
    }

    public GameSettings getSettings() {
        return settings;
    }
    public GameModeEnum getGameMode() {
        return gameMode;
    }
    public QueuePlayerOrganizer getOrganizer() {
        return organizer;
    }
    public Player getOwner() {
        return owner;
    }
}
