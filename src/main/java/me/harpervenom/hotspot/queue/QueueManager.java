package me.harpervenom.hotspot.queue;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameListener;
import me.harpervenom.hotspot.game.GameModeEnum;
import me.harpervenom.hotspot.player.GamePlayer;
import me.harpervenom.hotspot.player.PlayerManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.text;

public class QueueManager implements GameListener {

    private final PlayerManager playerManager;

    private final List<GameQueue> gameQueues = new ArrayList<>();
    private final List<QueueListener> listeners = new ArrayList<>();

    private final HashMap<Player, GameQueue> playerQueues = new HashMap<>();

    public QueueManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public void createQueue(GameModeEnum mode) {
        GameQueue queue = new GameQueue(this, mode);
        gameQueues.add(queue);

        for (QueueListener l : listeners) l.onQueueCreate(queue);
    }

    public void removeQueue(GameQueue queue) {
        gameQueues.remove(queue);

        for (Player player : new ArrayList<>(queue.getPlayers())) {
            removePlayerFromQueue(player);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            createQueue(queue.getGameMode());
        }, 3 * 20);

        for (QueueListener l : listeners) l.onQueueRemove(queue);
    }

    public void readyQueue(GameQueue gameQueue) {
        for (QueueListener l : listeners) l.onQueueReady(gameQueue);
    }

    public boolean addPlayerToQueue(Player player, GameQueue queue) {
        if (getQueue(player) != null) return false;
        queue.addPlayer(player);
        playerQueues.put(player, queue);
        for (QueueListener l : listeners) l.onPlayerJoin(player, queue);
        return true;
    }

    public void removePlayerFromQueue(Player player) {
        GameQueue queue = getQueue(player);
        if (queue == null) return;
        queue.removePlayer(player);
        playerQueues.remove(player);
        for (QueueListener l : listeners) l.onPlayerLeave(player, queue);

//        if (player.isOnline()) {
//            player.sendMessage(text("Вы покинули очередь", NamedTextColor.RED));
//            player.sendActionBar(text(""));
//        }
    }

    public GameQueue getQueue(Player player) {
        return playerQueues.get(player);
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
