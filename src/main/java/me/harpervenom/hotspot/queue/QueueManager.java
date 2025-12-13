package me.harpervenom.hotspot.queue;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameListener;
import me.harpervenom.hotspot.game.GameModeEnum;
import me.harpervenom.hotspot.game.map.MapManager;
import me.harpervenom.hotspot.menu.components.Window;
import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class QueueManager implements GameListener {

    public enum AddPlayerResult {
        SUCCESS,              // Joined successfully
        ALREADY_JOINED,       // Already in this queue (with same team or can't change)
        NOT_ALLOWED           // Can't join for any other reason (full, can't accept, etc.)
    }

    private final MapManager mapManager;

    private final List<GameQueue> gameQueues = new ArrayList<>();
    private final List<QueueListener> listeners = new ArrayList<>();

    private final HashMap<Player, GameQueue> playerQueues = new HashMap<>();
    private final HashMap<Player, GameQueue> queueOwners = new HashMap<>();
    private final HashMap<GameQueue, Window> queueWindows = new HashMap<>();

    public QueueManager(MapManager mapManager) {
        this.mapManager = mapManager;
    }

    public GameQueue createQueue(GameModeEnum mode) {
        return createQueue(mode, null);
    }

    public GameQueue createQueue(GameModeEnum mode, Player owner) {
        if (owner != null) {
            clearQueue(owner);
        }
        GameQueue queue = new GameQueue(this, mode, owner);

        mode.getSettings().setMapData(mapManager.getMaps().getFirst());
        queue.updateScoreboard();

        if (owner != null) {
            queue.addViewer(owner);
            queueOwners.put(owner, queue);
        }
        gameQueues.add(queue);

        for (QueueListener l : listeners) l.onQueueCreate(queue);
        return queue;
    }

    public void setWindow(GameQueue queue, Window window) {
        queueWindows.put(queue, window);
    }

    public void removeQueue(Player owner) {
        GameQueue queue = queueOwners.remove(owner);
        queue.removeViewer(owner);
        removeQueue(queue);
    }

    public void removeQueue(GameQueue queue) {
        queue.clean();
        if (queue.getOwner() != null) {
            queueOwners.remove(queue.getOwner());
        }
        for (Player player : new ArrayList<>(queue.getPlayers())) {
            removePlayerFromQueue(player, true);
        }

        if (!queue.getSettings().isCustom()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                createQueue(queue.getMode());
            }, 3 * 20);
        }

        gameQueues.remove(queue);
        for (QueueListener l : listeners) l.onQueueRemove(queue);
    }

    public void readyQueue(GameQueue gameQueue) {
        for (QueueListener l : listeners) l.onQueueReady(gameQueue);
    }

    public AddPlayerResult addPlayerToQueue(Player player, GameQueue queue, QueueTeam team) {
        GameQueue lastQueue = getQueue(player);

        if (queue.isFull()) {
            return AddPlayerResult.NOT_ALLOWED;
        }

        if (!queue.canAccept(player, team)) return AddPlayerResult.NOT_ALLOWED;

        if (lastQueue != null) {
            if (!lastQueue.equals(queue)) {
                clearQueue(player);
            } else if (!queue.getSettings().canChooseTeam() || (team != null && team.equals(lastQueue.getTeam(player)))) {
                return AddPlayerResult.ALREADY_JOINED;
            } else {
                removePlayerFromQueue(player, true);
            }
        }

        queue.addPlayer(player, team);
        queue.addViewer(player);

        playerQueues.put(player, queue);

        if (queue.getSettings().canChooseTeam()) {
            queueWindows.get(queue).update();
        }

        for (QueueListener l : listeners) l.onPlayerJoin(player, queue);
        return AddPlayerResult.SUCCESS;
    }

    public void removePlayerFromQueue(Player player) {
        removePlayerFromQueue(player, false);
    }
    public void removePlayerFromQueue(Player player, boolean silent) {
        GameQueue queue = getQueue(player);
        if (queue == null) return;
        queue.removePlayer(player, silent);
        if (!queue.isOwner(player)) {
            queue.removeViewer(player);
        }

        playerQueues.remove(player);

        if (silent) return;
        for (QueueListener l : listeners) l.onPlayerLeave(player, queue);
    }

    public void clearQueue(Player player) {
        if (queueOwners.containsKey(player)) {
            removeQueue(player);
        } else if (playerQueues.containsKey(player)) {
            removePlayerFromQueue(player);
        }
    }

    public GameQueue getQueue(Player player) {
        if (queueOwners.containsKey(player)) {
            return queueOwners.get(player);
        }
        return playerQueues.get(player);
    }

    public Window getQueueWindow(GameQueue queue) {
        return queueWindows.get(queue);
    }

    public void callTickEvent(GameQueue queue) {
        for (QueueListener l : listeners) l.onTimerTick(queue);
    }

    public void addListener(QueueListener queueListener) {
        listeners.add(queueListener);
    }

    public List<GameQueue> getQueues() {
        return gameQueues.stream()
                .sorted(Comparator.comparingInt(q -> modePriority(q.getMode())))
                .toList();
    }

    private static int modePriority(GameModeEnum mode) {
        return switch (mode) {
            case NORMAL -> 0;
            case RANKED -> 1;
            case CUSTOM -> 2;
        };
    }

    @Override
    public void onGameStart(Game game) {
        removeQueue(game.getQueue());
    }
}
