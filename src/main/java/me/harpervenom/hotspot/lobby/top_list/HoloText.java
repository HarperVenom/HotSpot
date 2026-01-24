package me.harpervenom.hotspot.lobby.top_list;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class HoloText {

    private static final Set<UUID> ACTIVE_DISPLAYS = new HashSet<>();

    private final List<UUID> lines = new ArrayList<>();
    private final Location baseLocation;

    private double extraSpacing = 0.0;
    private static final double DEFAULT_SPACING = 0.25;

    private static final NamespacedKey TAG_KEY =
            new NamespacedKey(plugin, "custom_text_display");

    public HoloText(Location baseLocation) {
        this.baseLocation = baseLocation.clone().add(0.5, 0.5, 0.5);
    }

    public void addLine(Component text) {
        Location loc;

        if (lines.isEmpty()) {
            loc = baseLocation.clone();
        } else {
            UUID lastId = lines.getLast();
            Entity e = Bukkit.getEntity(lastId);

            if (e instanceof TextDisplay last) {
                loc = last.getLocation().clone()
                        .subtract(0, DEFAULT_SPACING + extraSpacing, 0);
            } else {
                // Fallback if last line no longer exists
                loc = baseLocation.clone()
                        .subtract(0, (DEFAULT_SPACING + extraSpacing) * lines.size(), 0);
            }
        }

        TextDisplay display = (TextDisplay) baseLocation.getWorld()
                .spawnEntity(loc, EntityType.TEXT_DISPLAY);

        display.text(text);
        display.setBillboard(Display.Billboard.CENTER);
        display.setDefaultBackground(false);
        display.setSeeThrough(false);
        display.setShadowed(true);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setPersistent(true);
        display.setBrightness(new Display.Brightness(15, 15));

        display.getPersistentDataContainer()
                .set(TAG_KEY, PersistentDataType.BYTE, (byte) 1);

        UUID id = display.getUniqueId();
        lines.add(id);
        ACTIVE_DISPLAYS.add(id);

        extraSpacing = 0.0;
    }

    /**
     * Adds extra vertical spacing before the next line.
     */
    public void addLineSpacing(double space) {
        this.extraSpacing = space;
    }

    public void remove() {
        for (UUID id : lines) {
            Entity e = Bukkit.getEntity(id);

            if (e instanceof TextDisplay td) {
                Chunk chunk = td.getLocation().getChunk();
                if (!chunk.isLoaded()) {
                    chunk.load();
                }

                td.remove();
            }
            ACTIVE_DISPLAYS.remove(id);
        }

        lines.clear();
    }

    public static void cleanDisplays(World world) {
        for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
            if (ACTIVE_DISPLAYS.contains(entity.getUniqueId())) continue;
            if (entity instanceof TextDisplay td &&
                    td.getPersistentDataContainer().has(TAG_KEY, PersistentDataType.BYTE)) {
                td.remove();
            }
        }
    }
}
