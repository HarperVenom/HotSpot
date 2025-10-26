package me.harpervenom.hotspot.player;

import me.harpervenom.hotspot.queue.GameQueue;
import org.bukkit.entity.Player;

public class GamePlayer {

    private Player player;
    private GameQueue gameQueue;
//    private Game lastGame;

    public GamePlayer(Player player) {
        this.player = player;
    }

    public boolean hasQueue() {
        return gameQueue != null;
    }
    public void setQueue(GameQueue gameQueue) {
        this.gameQueue = gameQueue;
    }
    public GameQueue getQueue() {
        return gameQueue;
    }
//
//    public void setLastGame(Game lastGame) {
//        this.lastGame = lastGame;
//    }
//    public Game getLastGame() {
//        return lastGame;
//    }
//    public boolean hasLastGame() {
//        return lastGame != null;
//    }

    public void setPlayer(Player player) {
        this.player = player;
    }
    public Player getPlayer() {
        return player;
    }
}
