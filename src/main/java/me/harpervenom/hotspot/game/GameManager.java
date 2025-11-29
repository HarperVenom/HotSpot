package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.map.MapManager;
import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.queue.QueueListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class GameManager implements GameListener, QueueListener {

    private final MapManager mapManager;
    private final List<Game> games = new ArrayList<>();
    private final List<GameListener> listeners = new ArrayList<>();
//    private final PartyManager partyManager;

    public GameManager(MapManager mapManager) {
        this.mapManager = mapManager;
        listeners.add(this);
    }

    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    public void createGame(GameQueue queue) {
        Game game = new Game(this, queue);

        mapManager.createMap(game).thenAccept(map -> {
            if (map == null) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                game.setMap(map);
                game.setup();

                List<Player> players = queue.getPlayers();
                Collections.shuffle(players);

                for (Player player : players) {
                    game.connect(player);
                }

                game.start();
                listeners.forEach(l -> l.onGameStart(game));
                updateGames();
            });

            games.add(game);
        });
    }

    public void updateGames() {
        listeners.forEach(GameListener::onGamesUpdate);
    }

    public void removeGame(Game game) {
        if (!games.contains(game)) return;
        games.remove(game);

        mapManager.removeMap(game.getMap());

        game.remove();

        // Notify all listeners that the game ended
        listeners.forEach(l -> l.onGameEnd(game));
        updateGames();
    }

    public Game getGame(World world) {
        return games.stream()
                .filter(game -> game.getMap().getWorld().getUID().equals(world.getUID()))
                .findFirst()
                .orElse(null);
    }


    public Game getGame(Player player) {
        return games.stream()
                .filter(game -> game.getPlayers().stream()
                                .anyMatch(p -> p.equals(player)))
                .findFirst()
                .orElse(null);
    }

    public List<GameListener> getListeners() {
        return listeners;
    }

    public List<Game> getGames() {
        return Collections.unmodifiableList(games);
    }

    @Override
    public void onQueueReady(GameQueue queue) {
        createGame(queue);
    }

    public void close() {
        mapManager.close();
    }
}


