package me.harpervenom.hotspot.game.vault;

import me.harpervenom.hotspot.game.GameProfile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.game.vault.VaultManager.removeItemOwner;
import static me.harpervenom.hotspot.game.vault.VaultManager.setItemOwner;

public class Vault {

    private final Block dropper;

    private final List<Player> rewarded = new ArrayList<>();
    private final List<Player> rewarding = new ArrayList<>();

    public Vault(Location baseLocation) {
        dropper = baseLocation.getBlock();
    }

    public void build() {
        dropper.setType(Material.DISPENSER);
        BlockData data = dropper.getBlockData();
        if (data instanceof Directional directional) {
            directional.setFacing(BlockFace.UP);
            dropper.setBlockData(directional);
        }
    }

    public void open(GameProfile profile) {
        Player player = profile.getPlayer();

        if (rewarded.contains(player)) {
            dropper.getWorld().playSound(dropper.getLocation(), Sound.BLOCK_VAULT_INSERT_ITEM_FAIL, 1, 1.2f);
            return;
        }
        if (rewarding.contains(player)) return;

        final int[] amount = {5};

        rewarding.add(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!rewarding.contains(player)) {
                    cancel();
                    return;
                }

                ItemStack egg = new ItemStack(Material.EGG);

                Item item = dispenseItem(egg);
                if (item != null) {
                    setItemOwner(item.getItemStack(), player.getUniqueId());
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        removeItemOwner(item.getItemStack());
                    }, 5 * 20);
//                    profile.addLoot(item.getItemStack());
                }

                amount[0]--;

                if (amount[0] == 0) {
                    rewarding.remove(player);

                    if (!notRewarded.contains(player)) {
                        rewarded.add(player);
                    } else {
                        notRewarded.remove(player);
                    }

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    private Item dispenseItem(ItemStack itemStack) {
        Location spawnLocation = dropper.getLocation().clone().add(0.5, 1, 0.5);

        World world = spawnLocation.getWorld();

        Item item = null;
        if (itemStack != null) {

//            if (itemStack.getType() == Material.ENCHANTED_BOOK) {
//                addLoreLine(itemStack, text("*ПКМ по нужному предмету в инвентаре*"));
//            }

            item = world.dropItem(spawnLocation, itemStack);
            item.setVelocity(new Vector(0, 0.15, 0));
            item.setPickupDelay(0);
        }

        world.spawnParticle(Particle.SMOKE, spawnLocation, 5, 0.1, 0.1, 0.1, 0.01);
        world.playSound(spawnLocation, Sound.BLOCK_VAULT_EJECT_ITEM, 1, 1.2f);

        return item;
    }

    private final List<Player> notRewarded = new ArrayList<>();

    public void resetForPlayer(Player player) {
        rewarded.remove(player);
    }

    public void reset() {
        rewarded.clear();
        notRewarded.addAll(rewarding);
    }

    public void stopRewarding(Player player) {
        rewarding.remove(player);
    }

    public boolean isBlock(Block block) {
        return (block.getLocation().equals(dropper.getLocation()));
    }
}
