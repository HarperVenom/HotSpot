package me.harpervenom.hotspot.game.profile;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EconomyManager {

    private final GameProfile profile;

    private int balance;

    private double lifeDamage = 0;
    private double lastDealtDamage;

    public EconomyManager(GameProfile profile) {
        this.profile = profile;
    }

    public void transferToBalance(double damage) {
        double multiplier = 1;

//        dealtDamage += damage;

        lastDealtDamage += multiplier * damage;

        if (lastDealtDamage > 2.0) {
            double remaining = lastDealtDamage % 2.0;
            updateBalance((int) lastDealtDamage / 2);
            lastDealtDamage = remaining;
        }

        lifeDamage += multiplier * damage;
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
