package me.harpervenom.hotspot.game.trader;

import me.harpervenom.hotspot.game.profile.GameProfile;
import me.harpervenom.hotspot.menu.components.Button;
import me.harpervenom.hotspot.menu.components.Window;
import org.apache.logging.log4j.util.Supplier;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import static me.harpervenom.hotspot.game.trader.TradeType.*;

public class TraderWindow extends Window {

    private final GameProfile profile;

    public TraderWindow(GameProfile profile) {
        super("Магазин", 27);
        this.profile = profile;

        update();
    }

    public void update() {
        clear();

        Button pickaxeButton = new Button(PICKAXE.getShopItemStack(profile));
        setUpgradeButton(pickaxeButton, () -> PICKAXE.upgrade(profile));

        Button foodButton = new Button(FOOD.getShopItemStack(profile));
        setUpgradeButton(foodButton, () -> FOOD.upgrade(profile));

        Button blocksButton = new Button(BLOCKS.getShopItemStack(profile));
        setUpgradeButton(blocksButton, () -> BLOCKS.upgrade(profile));

        Button helmetButton = new Button(HELMET.getShopItemStack(profile));
        setUpgradeButton(helmetButton, () -> HELMET.upgrade(profile));

        Button leggingsButton = new Button(LEGGINGS.getShopItemStack(profile));
        setUpgradeButton(leggingsButton, () -> LEGGINGS.upgrade(profile));

        Button bootsButton = new Button(BOOTS.getShopItemStack(profile));
        setUpgradeButton(bootsButton, () -> BOOTS.upgrade(profile));


        addButton(pickaxeButton, 10);
        addButton(foodButton, 11);
        addButton(blocksButton, 12);

        addButton(helmetButton, 14);
        addButton(leggingsButton, 15);
        addButton(bootsButton, 16);
    }

    private void setUpgradeButton(Button button, Supplier<Boolean> upgradeLogic) {
        button.setOnClick(() -> {
            boolean success = upgradeLogic.get();

            Player player = profile.getPlayer();

            if (!success) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                update();
            }
        });
    }
}
