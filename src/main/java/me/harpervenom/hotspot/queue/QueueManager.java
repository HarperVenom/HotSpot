package me.harpervenom.hotspot.queue;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameListener;
import me.harpervenom.hotspot.game.GameModeEnum;
import me.harpervenom.hotspot.menu.components.Window;
import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class QueueManager implements GameListener {

    private final List<GameQueue> gameQueues = new ArrayList<>();
    private final List<QueueListener> listeners = new ArrayList<>();

    private final HashMap<Player, GameQueue> playerQueues = new HashMap<>();
    private final HashMap<Player, GameQueue> queueOwners = new HashMap<>();
    private final HashMap<GameQueue, Window> queueWindows = new HashMap<>();

    public GameQueue createQueue(GameModeEnum mode) {
        return createQueue(mode, null);
    }

    public GameQueue createQueue(GameModeEnum mode, Player owner) {
        if (owner != null) {
            clearQueue(owner);
        }
        GameQueue queue = new GameQueue(this, mode, owner);
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
        gameQueues.remove(queue);
        queue.clean();
        if (queue.getOwner() != null) {
            queueOwners.remove(queue.getOwner());
        }
        for (Player player : new ArrayList<>(queue.getPlayers())) {
            queue.removePlayer(player, true);
            playerQueues.remove(player);
        }

        if (!queue.getSettings().isCustom()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                createQueue(queue.getGameMode());
            }, 3 * 20);
        }

        for (QueueListener l : listeners) l.onQueueRemove(queue);
    }

    public void readyQueue(GameQueue gameQueue) {
        for (QueueListener l : listeners) l.onQueueReady(gameQueue);
    }

    public boolean addPlayerToQueue(Player player, GameQueue queue) {
        return addPlayerToQueue(player, queue, null);
    }

    public boolean addPlayerToQueue(Player player, GameQueue queue, QueueTeam team) {
        GameQueue lastQueue = getQueue(player);
        if (lastQueue != null) {
            if (!lastQueue.equals(queue)) {
                clearQueue(player);
            } else if (team != null && team.equals(lastQueue.getTeam(player))) {
                return false;
            } else {
                removePlayerFromQueue(player, true);
            }
        }

        queue.addPlayer(player, team);
        queue.addViewer(player);

        playerQueues.put(player, queue);

        if (queue.getSettings().isCustom()) {
            queueWindows.get(queue).update();
        }

        for (QueueListener l : listeners) l.onPlayerJoin(player, queue);
        return true;
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

        for (QueueListener l : listeners) l.onPlayerLeave(player, queue);

        playerQueues.remove(player);
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

    public void addListener(QueueListener queueListener) {
        listeners.add(queueListener);
    }

    public List<GameQueue> getQueues() {
        return gameQueues;
    }
    @Override
    public void onGameStart(Game game) {
        removeQueue(game.getQueue());
    }
}
