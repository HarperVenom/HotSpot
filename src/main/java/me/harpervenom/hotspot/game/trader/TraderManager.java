package me.harpervenom.hotspot.game.trader;

import me.harpervenom.hotspot.game.Game;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TraderManager {

    private final Game game;

    private final HashMap<UUID, Trader> traders = new HashMap<>();

    public TraderManager(Game game) {
        this.game = game;
    }

    public void setup() {
        for (Trader trader : game.getMap().getTraders()) {
            Villager villager = trader.spawn();
            traders.put(villager.getUniqueId(), trader);
        }
    }

    public Trader getTrader(Entity entity) {
        return traders.get(entity.getUniqueId());
    }
}
