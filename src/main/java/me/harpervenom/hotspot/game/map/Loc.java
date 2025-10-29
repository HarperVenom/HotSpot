package me.harpervenom.hotspot.game.map;

import org.bukkit.block.BlockFace;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class Loc {

    public final int x, y, z;
    public final float yaw;

    public Loc(int x, int y, int z, String facing) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = getYaw(facing);
    }

    private static float getYaw(String facing) {
        if (facing == null) return 0f;
        try {
            BlockFace face = BlockFace.valueOf(facing);
            return getYawFromBlockFace(face);
        } catch (IllegalArgumentException e) {
            // Log the error and return a default yaw (e.g., 0)
            plugin.getLogger().warning("Invalid BlockFace: " + facing);
            return 0f;
        }
    }

    private static float getYawFromBlockFace(BlockFace face) {
        return switch (face) {
            case NORTH -> 180.0f;
            case EAST -> -90.0f;
            case SOUTH -> 0.0f;
            case WEST -> 90.0f;
            case NORTH_EAST -> -135.0f;
            case NORTH_WEST -> 135.0f;
            case SOUTH_EAST -> -45.0f;
            case SOUTH_WEST -> 45.0f;
            default -> 0.0f; // Default to SOUTH if face is not recognized
        };
    }
}
