package me.harpervenom.hotspot.lobby.top_list;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

import java.util.Map;

import static net.kyori.adventure.text.Component.text;

public class TopList {

    private final Map<Component, Component> lines;
    private final HoloText holoText;
    private final String title;

    public TopList(Location location, Map<Component, Component> lines, String title) {
        this.lines = lines;
        this.holoText = new HoloText(location);
        this.title = title;

        holoText.addLineSpacing(0.15);
        holoText.addLine(text(title, NamedTextColor.GOLD));
    }

    public void generate() {
        int rank = 1;
        for (Map.Entry<Component, Component> entry : lines.entrySet()) {
            Component name = entry.getKey();
            Component score = entry.getValue();

            Component line = text(rank + ". ", NamedTextColor.YELLOW)
                    .append(name)
                    .append(text(" - ", NamedTextColor.GRAY))
                    .append(score);

            holoText.addLine(line);
            rank++;
        }

        // Fill up to 10 entries with empty ones if needed
        if (rank == 1) {
            holoText.addLine(text("-"));
        }
    }

    public void remove() {
        holoText.remove();
    }
}
