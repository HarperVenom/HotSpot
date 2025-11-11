package me.harpervenom.hotspot.game.profile;

import org.bukkit.entity.Player;

public class EconomyManager {

    private final GameProfile profile;

    private int balance;

    public EconomyManager(GameProfile profile) {
        this.profile = profile;
    }

    public boolean takePayment(int payment) {
        if (payment > getBalance()) return false;

        updateBalance(-payment);
        return true;
    }

    public void updateBalance(int update) {
        Player player = profile.getPlayer();
        if (player == null) return;
        balance += update;
        player.setLevel(balance);
    }

    public int getBalance() {
        if (balance == 0) {
            balance = profile.getPlayer().getLevel();
        }
        return balance;
    }
}
