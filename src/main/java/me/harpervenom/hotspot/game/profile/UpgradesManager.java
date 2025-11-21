package me.harpervenom.hotspot.game.profile;

import me.harpervenom.hotspot.game.trader.TradeType;

import java.util.HashMap;
import java.util.Map;

public class UpgradesManager {

    private final GameProfile profile;
    private final Map<TradeType, Integer> tradeLevels = new HashMap<>();

    public UpgradesManager(GameProfile profile) {
        this.profile = profile;
    }

    public void setLevel(TradeType type, int level) {
        tradeLevels.put(type, level);
    }

    public void increaseLevel(TradeType type) {
        int currentLevel = getTradeLevel(type);
        if (currentLevel < type.getMaxLevel()) {

            tradeLevels.put(type, currentLevel + 1);

            if (type.getArmorSlot() != null) {
                profile.getEquipmentManager().replaceArmorItem(type);
            } else {
                profile.getEquipmentManager().replaceItem(type);
            }
        }
    }

    public int getTradeLevel(TradeType type) {
        return tradeLevels.getOrDefault(type, 0);
    }
}
