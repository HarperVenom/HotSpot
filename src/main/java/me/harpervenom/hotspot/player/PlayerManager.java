package me.harpervenom.hotspot.player;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {

    private final HashMap<UUID, GamePlayer> gamePlayers = new HashMap<>();

    public PlayerManager() {

    }

//    public void updateButtonSet(GamePlayer gamePlayer, ButtonSet buttonSet) {
//        gamePlayer.setButtonSet(buttonSet);
//        updateInventory(gamePlayer);
//    }
//
//    public void updateButton(GamePlayer gamePlayer, int slot, Button button) {
//        gamePlayer.updateButton(slot, button);
//        updateInventory(gamePlayer);
//    }
//
//    private void updateInventory(GamePlayer gamePlayer) {
//        Player player = gamePlayer.getPlayer();
//        Map<Integer, Button> buttons = gamePlayer.getButtonSet().getButtons();
//        Inventory inventory = player.getInventory();
//
//        // iterate through all button slots
//        for (int i = 0; i < inventory.getSize(); i++) {
//            Button button = buttons.get(i);
//            ItemStack item = inventory.getItem(i);
//            if (button == null) {
//                // only clear the slot if it’s not already air
//                if (item != null && item.getType() != Material.AIR) {
//                    inventory.setItem(i, new ItemStack(Material.AIR));
//                }
//                continue;
//            }
//
//            // skip if the item is already identical
//            if (button.getItemStack().isSimilar(inventory.getItem(i))) continue;
//
//            // update the slot
//            inventory.setItem(i, button.getItemStack());
//        }
//    }


    public GamePlayer get(Player player) {
        GamePlayer p = gamePlayers.get(player.getUniqueId());
        if (p == null) {
            p = new GamePlayer(player);
            gamePlayers.put(player.getUniqueId(), p);
        }
        p.setPlayer(player);
        return p;
    }
}
