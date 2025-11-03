package me.harpervenom.hotspot.utils;

import java.util.HashMap;
import java.util.Map;

public class FontWidth {

    private static final Map<Character, Integer> WIDTHS = new HashMap<>();

    static {
        // Default letters & numbers are 6px wide, except:
        // Narrow characters = 4px wide
        // Punctuation & special characters vary
        // Space = 4px (Minecraft uses 4px logical)

        // Narrow
        add(".,:;|!i\'`l", 2);

        // Very narrow
        add(" ", 4);

        // Wide-ish
        add("@~", 7);

        // Extra wide
        add("W", 8);

        // Default width 6px for everything else
    }

    private static void add(String chars, int width) {
        for (char c : chars.toCharArray()) {
            WIDTHS.put(c, width);
        }
    }

    public static int getCharWidth(char c) {
        return WIDTHS.getOrDefault(c, 6); // Default width 6px
    }

    public static int getStringWidth(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            width += getCharWidth(c) + 1; // +1 typical spacing
        }
        return width;
    }
}

