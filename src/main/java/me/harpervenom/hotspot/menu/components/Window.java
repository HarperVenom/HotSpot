package me.harpervenom.hotspot.menu.components;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Window {

    public static HashMap<Player, Window> openedWindow = new HashMap<>();

    private final Inventory inventory;
    private final Map<Integer, Button> buttons = new HashMap<>(); // slot, button
    private Runnable onUpdate;
    private Runnable onClose;

    private final Set<Player> viewers = new HashSet<>();

    public Window(String name, InventoryType type) {
        inventory = Bukkit.createInventory(null, type, Component.text(name));
    }

    public Window(String name, int slots) {
        inventory = Bukkit.createInventory(null, slots, Component.text(name));
    }

    public void addButton(Button button, int slot) {
        buttons.put(slot, button);
        inventory.setItem(slot, button.getItemStack());
    }

    public void setOnUpdate(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void update() {
        onUpdate.run();
    }

    public void open(Player player) {
        if (inventory == null) return;
        player.openInventory(inventory);
        openedWindow.put(player, this);
        viewers.add(player);
    }

    public void close(Player player) {
        close(player, true);
    }

    public void close(Player player, boolean callCloseInventory) {
        if (callCloseInventory) {
            player.closeInventory();
        }

        openedWindow.remove(player);
        viewers.remove(player);

        if (onClose != null) {
            onClose.run();
        }
    }

    public void closeForAll() {
        for (Player player : new HashSet<>(viewers)) {
            if (player.isOnline()) {
                player.closeInventory();
            }
            openedWindow.remove(player);
        }

        viewers.clear();

        if (onClose != null) {
            onClose.run();
        }
    }

    public void click(int slot, Player player, boolean isShifting) {
        Button button = buttons.get(slot);
        if (button == null) return;
        button.click(player, isShifting);
    }

    public void clear() {
        inventory.clear();
        buttons.clear();
    }
}
