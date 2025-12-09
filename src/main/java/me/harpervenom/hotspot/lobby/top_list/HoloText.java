package me.harpervenom.hotspot.lobby.top_list;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class HoloText {

    private final List<TextDisplay> lines = new ArrayList<>();
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
            TextDisplay last = lines.getLast();
            loc = last.getLocation().clone().subtract(0, DEFAULT_SPACING + extraSpacing, 0);
        }

        TextDisplay display = (TextDisplay) baseLocation.getWorld()
                .spawnEntity(loc, EntityType.TEXT_DISPLAY);

        display.text(text);
        display.setBillboard(Display.Billboard.CENTER);
        display.setDefaultBackground(false);
        display.setSeeThrough(false);
        display.setShadowed(true);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setPersistent(false);
        display.setBrightness(new Display.Brightness(15, 15));

        display.getPersistentDataContainer()
                .set(TAG_KEY, PersistentDataType.BYTE, (byte) 1);

        lines.add(display);

        extraSpacing = 0.0;
    }

    /**
     * Adds extra vertical spacing before the next line.
     */
    public void addLineSpacing(double space) {
        this.extraSpacing = space;
    }

    public void setLine(int index, Component newText) {
        if (index >= 0 && index < lines.size()) {
            lines.get(index).text(newText);
        }
    }

    public List<TextDisplay> getLines() {
        return lines;
    }

    public void remove() {
        for (TextDisplay display : lines) {
            display.remove();
        }
        lines.clear();
    }

    /**
     * Removes all tagged TextDisplays (e.g. on reload).
     */
    public static void removeTaggedTextDisplays() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(TextDisplay.class)) {
                if (entity.getPersistentDataContainer()
                        .has(TAG_KEY, PersistentDataType.BYTE)) {
                    entity.remove();
                }
            }
        }
    }
}
