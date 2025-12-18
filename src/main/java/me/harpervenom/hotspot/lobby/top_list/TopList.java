package me.harpervenom.hotspot.lobby.top_list;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Map;

import static net.kyori.adventure.text.Component.text;

public class TopList {

    private final Map<Component, Component> lines;
    private final HoloText holoText;

    public TopList(Location location, Map<Component, Component> lines, String title) {
        this.lines = lines;
        this.holoText = new HoloText(location);

        holoText.addLineSpacing(0.15);
        holoText.addLine(text(title, NamedTextColor.GOLD));
    }

    public void setLines(Map<Component, Component> newLines) {
        this.lines.clear();
        this.lines.putAll(newLines);
    }

    public void update() {
        // Remove old lines except title
        int linesToRemove = holoText.getLines().size() - 1;
        for (int i = 0; i < linesToRemove; i++) {
            holoText.getLines().getLast().remove();
            holoText.getLines().removeLast();
        }

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

    public HoloText getHoloText() {
        return holoText;
    }
}
