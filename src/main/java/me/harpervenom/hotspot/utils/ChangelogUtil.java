package me.harpervenom.hotspot.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChangelogUtil {

    private static List<String> changelogLines = new ArrayList<>();

    // Load file once (e.g. on plugin enable)
    public static void loadChangelog(JavaPlugin plugin) {
        try (InputStream in = plugin.getResource("CHANGELOG.md")) {
            if (in == null) {
                plugin.getLogger().warning("No CHANGELOG.md found in resources!");
                return;
            }
            changelogLines = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .toList();
        } catch (IOException e) {
            plugin.getLogger().severe("Error reading changelog: " + e.getMessage());
        }
    }

    // Send to player
    public static void sendChangelog(Player player) {
        if (changelogLines.isEmpty()) {
            player.sendMessage(Component.text("Нет доступного changelog.", NamedTextColor.RED));
            return;
        }

        for (String line : changelogLines) {
            Component comp = parseMarkdownLine(line);
            player.sendMessage(comp);
        }
    }

    // Very simple markdown → colors
    private static Component parseMarkdownLine(String line) {
        if (line.startsWith("# ")) {
            return Component.text(line.substring(2), NamedTextColor.GOLD).decorate(TextDecoration.BOLD);
        } else if (line.startsWith("## ")) {
            return Component.text(line.substring(3), NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
        } else if (line.startsWith("### ")) {
            return Component.text(line.substring(4), NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
        } else if (line.startsWith("- ")) {
            return Component.text("• " + line.substring(2), NamedTextColor.WHITE);
        }
        return Component.text(line, NamedTextColor.GRAY);
    }
}

