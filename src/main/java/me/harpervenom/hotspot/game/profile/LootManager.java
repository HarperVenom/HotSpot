package me.harpervenom.hotspot.game.profile;

import me.harpervenom.hotspot.game.vault.loot.LootClass;
import me.harpervenom.hotspot.game.vault.loot.LootEntry;

import java.util.HashMap;
import java.util.Map;

public class LootManager {

    private final GameProfile profile;

    private LootClass lootClass;
    private final Map<String, Integer> received = new HashMap<>();

    public LootManager(GameProfile profile) {
        this.profile = profile;
    }

    public void reset() {
        received.clear();
        lootClass = LootClass.getRandomClass(lootClass);
    }

    public void setLootClass(LootClass lootClass) {
        this.lootClass = lootClass;
    }
    public LootClass getLootClass() {
        return lootClass;
    }

    public int getReceivedCount(String key) {
        return received.getOrDefault(key, 0);
    }
    public void addReceived(LootEntry entry) {
        received.merge(entry.getKey(), 1, Integer::sum);
    }
}
