package me.harpervenom.hotspot.lobby.top_list;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;

public class TopList {

    private Map<Component, Component> lines;
    private final HoloText holoText;
    private final String title;

    public TopList(Location location, Map<Component, Component> lines, String title) {
        this.lines = lines;
        this.holoText = new HoloText(location);
        this.title = title;

        holoText.addLineSpacing(0.15);
        update();
    }

    public void update() {
        List<Component> components = new ArrayList<>();

        // Title is always line 0
        components.add(text(title, NamedTextColor.GOLD));

        int rank = 1;
        for (Map.Entry<Component, Component> entry : lines.entrySet()) {
            Component line = text(rank + ". ", NamedTextColor.YELLOW)
                    .append(entry.getKey())
                    .append(text(" - ", NamedTextColor.GRAY))
                    .append(entry.getValue());

            components.add(line);
            rank++;
        }

        // If no entries, show placeholder line
        if (rank == 1) {
            components.add(text("-", NamedTextColor.GRAY));
        }

        holoText.setLines(components);
    }

    /**
     * Replace data and refresh display.
     */
    public void updateLines(Map<Component, Component> newLines) {
        this.lines = newLines;
        update();
    }
}
