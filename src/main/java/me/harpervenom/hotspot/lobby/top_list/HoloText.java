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

    private final List<UUID> lines = new ArrayList<>();
    private final Location baseLocation;

    private double extraSpacing = 0.0;
    private static final double DEFAULT_SPACING = 0.25;

    private static final NamespacedKey TAG_KEY =
            new NamespacedKey(plugin, "custom_text_display");

    public HoloText(Location baseLocation) {
        this.baseLocation = baseLocation.clone().add(0.5, 0.5, 0.5);
    }

    public void setLine(int index, Component text) {
        TextDisplay display;

        if (index < lines.size()) {
            display = getDisplay(lines.get(index));
        } else {
            display = spawnLine(index);
            lines.add(display.getUniqueId());
        }

        if (display != null) {
            display.text(text);
        }
    }

    public void setLines(List<Component> components) {
        for (int i = 0; i < components.size(); i++) {
            setLine(i, components.get(i));
        }

        // Hide unused lines (never remove)
        for (int i = components.size(); i < lines.size(); i++) {
            TextDisplay td = getDisplay(lines.get(i));
            if (td != null) {
                td.text(Component.empty());
            }
        }
    }

    private TextDisplay getDisplay(UUID uuid) {
        Entity e = Bukkit.getEntity(uuid);
        return (e instanceof TextDisplay td) ? td : null;
    }

    private TextDisplay spawnLine(int index) {
        Location loc = baseLocation.clone()
                .subtract(0, (DEFAULT_SPACING + extraSpacing) * index, 0);

        TextDisplay display = (TextDisplay) baseLocation.getWorld()
                .spawnEntity(loc, EntityType.TEXT_DISPLAY);

        display.setBillboard(Display.Billboard.CENTER);
        display.setDefaultBackground(false);
        display.setSeeThrough(false);
        display.setShadowed(true);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setPersistent(true);
        display.setBrightness(new Display.Brightness(15, 15));

        display.getPersistentDataContainer()
                .set(TAG_KEY, PersistentDataType.BYTE, (byte) 1);

        extraSpacing = 0.0;
        return display;
    }

    public void addLineSpacing(double space) {
        this.extraSpacing = space;
    }

        public static void cleanDisplays(World world) {
        for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
            if (entity instanceof TextDisplay td &&
                    td.getPersistentDataContainer().has(TAG_KEY, PersistentDataType.BYTE)) {
                td.remove();
            }
        }
    }
}
