package me.harpervenom.hotspot.game.profile;

import me.harpervenom.hotspot.game.team.GameTeam;
import me.harpervenom.hotspot.game.trader.TradeType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

import static me.harpervenom.hotspot.utils.Utils.text;

public class GameProfile {

    private final UUID playerId;
    private GameTeam gameTeam;
    private boolean isConnected = true;

    private final UpgradesManager upgradesManager;
    private final EconomyManager economyManager;
    private final EquipmentManager equipmentManager;
    private final LootManager lootManager;

    public GameProfile(Player player) {
        this.playerId = player.getUniqueId();
        upgradesManager = new UpgradesManager(this);
        economyManager = new EconomyManager(this);
        equipmentManager = new EquipmentManager(this);
        lootManager = new LootManager(this);

        upgradesManager.setLevel(TradeType.PICKAXE, 1);

        reset();
    }

    public void reset() {
        lootManager.reset();
    }

    public void setTeam(GameTeam gameTeam) {
        this.gameTeam = gameTeam;
    }
    public GameTeam getTeam() {
        return gameTeam;
    }
    public Player getPlayer() {
        return Bukkit.getPlayer(playerId);
    }
    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
    public boolean isConnected() {
        return isConnected;
    }
    public Component getName() {
        return text(getPlayer().getName(), gameTeam.getColor());
    }

    public UpgradesManager getUpgradesManager() {
        return upgradesManager;
    }
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    public EquipmentManager getEquipmentManager() {
        return equipmentManager;
    }
    public LootManager getLootManager() {
        return lootManager;
    }
}
