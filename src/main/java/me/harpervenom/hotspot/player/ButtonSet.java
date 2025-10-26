package me.harpervenom.hotspot.player;

import me.harpervenom.hotspot.menu.components.Button;

import java.util.HashMap;

public class ButtonSet {

    private final HashMap<Integer, Button> buttons = new HashMap<>();

    public void setButton(int slot, Button button) {
        buttons.put(slot, button);
    }

    public HashMap<Integer, Button> getButtons() {
        return buttons;
    }
}
