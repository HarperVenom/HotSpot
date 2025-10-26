package me.harpervenom.hotspot.game;

public class GameSettings {

    private int maxPlayers;

//    private GameModeEnum mode;

    public GameSettings(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

//    public GameModeEnum getMode() {
//        return mode;
//    }
}
