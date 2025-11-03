package me.harpervenom.hotspot.game.team;

import org.bukkit.Location;

public class TeamBase {

    private Location spawn;
    private GameTrader trader;

    public TeamBase(Location spawn, GameTrader trader) {
        this.spawn = spawn;
        this.trader = trader;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }
    public Location getSpawn() {
        return spawn;
    }

    public void setTrader(GameTrader trader) {
        this.trader = trader;
    }
    public GameTrader getTrader() {
        return trader;
    }
}
