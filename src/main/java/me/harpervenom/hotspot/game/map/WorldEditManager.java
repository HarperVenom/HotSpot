package me.harpervenom.hotspot.game.map;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class WorldEditManager {

    private final HashMap<MapData, Clipboard> clipboards = new HashMap<>();

    public WorldEditManager() {

    }

    public void loadSchematic(File file, MapData mapData) {
        File schemFile = new File(file, File.separator + mapData.getName() + ".schem");

        if (!schemFile.exists()) {
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
        if (format == null) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(schemFile);
             ClipboardReader reader = format.getReader(fis)) {
            Clipboard clipboard = reader.read();
            clipboards.put(mapData, clipboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> pasteSchematicAsync(MapData mapData, World world) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Clipboard clipboard = clipboards.get(mapData);

        if (clipboard == null) {
            future.completeExceptionally(new IllegalStateException("Clipboard not loaded for mapData"));
            return future;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(world.getName());

                try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession) // <--- pass the actual EditSession
                            .to(BlockVector3.at(0, 0, 0))
                            .ignoreAirBlocks(false)
                            .build();

                    Operations.complete(operation);
                }

                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
                e.printStackTrace();
            }
        });

        return future;
    }


    public CompletableFuture<Void> clearWorldAsync(World world) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(world.getName());

                try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                    BlockVector3 min = BlockVector3.at(-300, -64, -300); // adjust as needed
                    BlockVector3 max = BlockVector3.at(300, world.getMaxHeight(), 300);

                    editSession.setBlocks((Region) new CuboidRegion(weWorld, min, max), BlockTypes.AIR.getDefaultState());
                }

//                plugin.getLogger().info("(" + world.getName() + ") blocks cleared.");

                // Now run the entity removal and complete the future on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    // Remove all entities except players
                    world.getEntities().stream()
                            .filter(entity -> !(entity instanceof Player))
                            .forEach(Entity::remove);

//                    plugin.getLogger().info("(" + world.getName() + ") entities cleared.");

                    future.complete(null); // signal completion
                });

            } catch (Exception e) {
                plugin.getLogger().warning("WorldEdit error during async clear");
                e.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> future.completeExceptionally(e));
            }
        });

        return future;
    }
}
