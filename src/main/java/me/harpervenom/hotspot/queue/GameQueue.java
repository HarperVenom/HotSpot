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
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.*;

public class GameQueue {

    private final QueueManager queueManager;
    private final GameModeEnum gameMode;
    private final QueuePlayerOrganizer organizer;
    private final CustomScoreboard scoreboard;
    private final GameSettings settings;
    private final CountdownTimer timer;
    private boolean isReady;

    private boolean isSkipped = false;

    private final List<Player> viewers = new ArrayList<>();

    private final List<Player> skippingPlayers = new ArrayList<>();

    private final Player owner;

    public GameQueue(QueueManager queueManager, GameModeEnum mode) {
        this(queueManager, mode, null);
    }

    public GameQueue(QueueManager queueManager, GameModeEnum mode, Player owner) {
        this.queueManager = queueManager;
        scoreboard = new CustomScoreboard("queue", mode.getSettings().getName());
        scoreboard.setPadding(1);
        gameMode = mode;
        settings = mode.getSettings();
        timer = new CountdownTimer(settings.isCustom() ? 6 : 60,
                () -> {
                    sendTitle(text("Запуск...", NamedTextColor.YELLOW), text(""), getPlayers());
                    ready();
                },
                (seconds) -> {
                    updateScoreboard();
                    if (seconds <= 5) {
                        isSkipped = true;
                        sendTitle(text(seconds + "", NamedTextColor.YELLOW), text(""), getPlayers());
                        playSound(Sound.BLOCK_NOTE_BLOCK_COW_BELL, 0.5f, 0.5f, getPlayers());
                    }
                    queueManager.callTickEvent(this);
                }
        );

        this.owner = owner;

        if (mode.getSettings().canChooseTeam()) {
            organizer = new TeamQueueOrganizer(this);
        } else {
            organizer = new SimpleQueueOrganizer(this);
        }
    }

    private void ready() {
        isReady = true;
        updateScoreboard();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            queueManager.readyQueue(this);
        }, 1L);
    }

    public boolean canAccept(Player player, QueueTeam team) {
        return organizer.canAccept(player, team);
    }

    public boolean addPlayer(Player player) {
        return addPlayer(player, null);
    }

    public boolean addPlayer(Player player, QueueTeam team) {
        boolean success;
        if (team != null) {
            success = organizer.addPlayerToTeam(player, team);
        } else {
            success = organizer.addPlayer(player);
        }

        if (!success) return false;

        List<Player> players = organizer.getAllPlayers();

        if (!getSettings().isCustom()) {
            if (players.size() == settings.getMinPlayers()) {
                timer.start();
            } else if (players.size() >= 2) {
                if (timer.getTimeLeft() > 60) {
                    timer.setTimeLeft(60);
                }
            }
        }

        updateScoreboard();
        return true;
    }

    public void removePlayer(Player player, boolean silent) {
        organizer.removePlayer(player);

        skippingPlayers.remove(player);

        List<Player> players = organizer.getAllPlayers();

        if (!silent && players.isEmpty()) {
            timer.reset();
            isSkipped = false;
        }

        player.showTitle(Title.title(text(""), text("")));

        updateScoreboard();
    }

    public void toggleSkip(Player player) {
        if (!skippingPlayers.remove(player)) skippingPlayers.add(player);
    }

    public boolean isSkipping(Player player) {
        return skippingPlayers.contains(player);
    }

    public void checkSkips() {
        boolean canSkip;

        if (settings.isCustom()) {
            canSkip = skippingPlayers.contains(owner);

            if (!canSkip) {
                timer.reset();
                updateScoreboard();
                sendTitle(text(""), text(""), getPlayers());
                return;
            } else {
                timer.start();
            }
            return;
        }

        int numberSkipping = skippingPlayers.size();
        int totalPlayers = getPlayers().size();
        int numberPlayersNeeded = Math.max(settings.getMinPlayers(), (int) Math.ceil(totalPlayers * 0.8));
        canSkip = totalPlayers > 1 && numberSkipping >= numberPlayersNeeded;

        // tests
        canSkip = true;
        if (canSkip) {
            isSkipped = true;
            timer.skip(settings.isCustom() ? 5 : 3);
            updateScoreboard();
            sendActionBarMessage(text(""), getPlayers(true));
        } else {
            actionBarMessage(text("Пропуск ожидания " + numberSkipping + "/" + numberPlayersNeeded, NamedTextColor.YELLOW));
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

    public boolean isReady() {
        return isReady;
    }

    public boolean isOwner(Player player) {
        return player.equals(owner);
    }

    public int getMaxPlayers() {
        return settings.getMaxPlayers();
    }
    public boolean isFull() {
        return getPlayers(false).size() >= settings.getMaxPlayers();
    }

    public void addViewer(Player player) {
        viewers.add(player);
        scoreboard.setViewers(viewers);
    }

    public void removeViewer(Player player) {
        viewers.remove(player);
        scoreboard.setViewers(viewers);
    }

    public List<Player> getPlayers() {
        return getPlayers(true);
    }

    public List<Player> getPlayers(boolean withOwner) {
        List<Player> players = new ArrayList<>(organizer.getAllPlayers());

        if (withOwner && owner != null && !players.contains(owner)) {
            players.add(owner);
        }

        return players;
    }

    public void updateScoreboard() {
        Component timeLeftLine = isReady ? text("Запуск...") : text("До начала: " + formatTime(timer.getTimeLeft()));

        if (!isReady && settings.isCustom() && !timer.isRunning()) {
            timeLeftLine = text("Ожидание");
        }

        if (!isReady && !timer.isRunning() && !settings.isCustom()) {
            timeLeftLine = text("Мин. игроков: " + settings.getMinPlayers());
        }

        scoreboard.updateLines(List.of(
                text(""),
                text("Карта: ").append(text(settings.getMapData().getDisplayName(), NamedTextColor.YELLOW)),
                text(""),
                text("Игроки: ").append(text(organizer.getAllPlayers().size() + "/" + settings.getMaxPlayers(), NamedTextColor.YELLOW)),
                text(""),
                timeLeftLine,
                text("")
        ));
    }

    public void clean() {
        viewers.clear();
        scoreboard.setViewers(new ArrayList<>());
        timer.cancel();
    }

    public CountdownTimer getTimer() {
        return timer;
    }

    public GameSettings getSettings() {
        return settings;
    }
    public GameModeEnum getMode() {
        return gameMode;
    }
    public QueuePlayerOrganizer getOrganizer() {
        return organizer;
    }
    public Player getOwner() {
        return owner;
    }

    public boolean isSkipped() {
        return isSkipped;
    }
}
