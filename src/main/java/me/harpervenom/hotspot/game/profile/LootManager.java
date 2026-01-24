package me.harpervenom.hotspot.game.profile;

import me.harpervenom.hotspot.game.vault.loot.LootClass;
import me.harpervenom.hotspot.game.vault.loot.LootEntry;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootManager {

    private final GameProfile profile;

    private LootClass lootClass;
    private final Map<String, Integer> received = new HashMap<>();
    private final List<ItemStack> items = new ArrayList<>();

    public LootManager(GameProfile profile) {
        this.profile = profile;
    }

    public void reset() {
        received.clear();
        lootClass = LootClass.getRandomClass(lootClass);
        items.clear();
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
    public void addItem(ItemStack itemStack) {
        items.add(itemStack);
    }
    public List<ItemStack> getItems() {
        return items;
    }
}
