package me.schooltests.regenblocks;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.schooltests.regenblocks.regions.RegenRegion;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.function.Consumer;

public final class Util {
    private Util() {}

    public static boolean regionExistsAtChunk(Chunk chunk, ProtectedRegion region) {
        if (chunk == null || region == null) return false;

        int x = chunk.getX() << 4;
        int z = chunk.getZ() << 4;

        BlockVector px = new BlockVector(x, 0, z);
        BlockVector pz = new BlockVector(x + 15, 256, z + 15);

        ProtectedCuboidRegion fakeRegion = new ProtectedCuboidRegion("fakeRegion", px, pz);

        ApplicableRegionSet set = WorldGuardPlugin.inst().getRegionManager(chunk.getWorld()).getApplicableRegions(fakeRegion);
        return set.getRegions().contains(region);
    }

    public static Set<ProtectedRegion> getRegionsAt(Location l) {
        return WorldGuardPlugin.inst().getRegionManager(l.getWorld()).getApplicableRegions(l).getRegions();
    }

    public static RegenRegion getRegenRegionAtLocation(Location l) {
        RegenBlocks plugin = RegenBlocks.getPlugin(RegenBlocks.class);

        Set<ProtectedRegion> regionsAt = getRegionsAt(l);
        for (RegenRegion r : plugin.getAPI().getRegionMap().values()) {
            String regionWorld = r.getWorld().getName();
            String world = l.getWorld().getName();

            boolean equalWorld = regionWorld.equals(world) && r.isGlobal();
            boolean containsRegion = r.getRegion() != null && regionsAt.contains(r.getRegion());
            if (equalWorld || containsRegion)
                return r;
        }

        return null;
    }

    public static void save(File file, String data, Consumer<String> completion) {
        try {
            if (!file.exists()) {
                File f = file.getParentFile();
                if (f != null)
                    f.mkdirs();
                file.createNewFile();
            }

            Path path = file.toPath();
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
            fileChannel.truncate(0);

            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
            buffer.flip();

            fileChannel.write(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    completion.accept(new String(attachment.array(), StandardCharsets.UTF_8));
                    attachment.clear();
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("Error when saving file named " + file.getName());
                    exc.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean invalidInventoryClick(InventoryClickEvent event) {
        return event.getClickedInventory() == null
                || event.getClickedInventory().getName() == null
                || event.getClickedInventory().getName().isEmpty()
                || event.getSlot() < 0;
    }

    public static Integer toInt(String val, Integer defaultValue) {
        try {
            return Integer.valueOf(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer toInt(String val) {
        return toInt(val, null);
    }

    public static Boolean toBool(String val, Boolean defaultValue) {
        switch (val.toLowerCase()) {
            case "yes":
            case "y":
            case "true":
                return true;
            case "no":
            case "n":
            case "false":
                return false;
            default:
                return defaultValue;
        }
    }

    public static Boolean toBool(String val) {
        return toBool(val, null);
    }

    public static String color(String msg, Object... formats) {
        for (int i = 0; i < formats.length; i++) {
            msg = msg.replaceFirst("%s", formats[i].toString())
                    .replaceAll("%\\{" + i + "}", formats[i].toString());
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}