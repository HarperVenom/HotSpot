package me.harpervenom.hotspot.menu.components;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class Button {

    private final ItemStack itemStack;
    protected Consumer<Player> onPersonalClick;
    protected Consumer<Player> onPersonalShiftClick;
    private Runnable onClick;

    public Button(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setOnPersonalClick(Consumer<Player> click) {
        this.onPersonalClick = click;
    }

    public void setOnPersonalShiftClick(Consumer<Player> shiftClick) {
        this.onPersonalShiftClick = shiftClick;
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public void click() {
        if (onClick != null) {
            onClick.run();
        }
    }

    public void click(Player player, boolean shift) {
        if (shift && onPersonalShiftClick != null) {
            onPersonalShiftClick.accept(player);
        } else if (onPersonalClick != null) {
            onPersonalClick.accept(player);
        } else {
            click();
        }
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}


