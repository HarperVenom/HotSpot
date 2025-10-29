package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.map.MapData;

public class GameSettings {

    private MapData mapData;
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
