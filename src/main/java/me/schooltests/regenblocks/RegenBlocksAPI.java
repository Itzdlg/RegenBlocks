package me.schooltests.regenblocks;

import com.google.gson.reflect.TypeToken;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.schooltests.regenblocks.events.api.RegenerateEvent;
import me.schooltests.regenblocks.regions.RegenData;
import me.schooltests.regenblocks.regions.RegenRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class RegenBlocksAPI {
    private final Map<String, RegenRegion> regions = new HashMap<>();
    private final Map<String, BukkitTask> regenTasks = new HashMap<>();
    private final RegenBlocks plugin;

    RegenBlocksAPI(RegenBlocks plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads regenerating region data such as settings and missed blocks from a file.
     * @param file The file to load region data from. Must be of JSON file type.
     */
    public void loadRegions(File file) {
        if (!file.exists()) return;
        try {
            Type type = new TypeToken<List<RegenRegion>>() {}.getType();
            List<RegenRegion> res = RegenBlocks.GSON.fromJson(new FileReader(file), type);

            regions.clear();
            if (res == null) return;
            for (RegenRegion r : res)
                regions.put(r.getId().toLowerCase(), r);
        } catch (IOException e) { }

        for (RegenRegion r : regions.values()) {
            regen(r, true);
            if (!regenTasks.containsKey(r.getId()))
                startRegenTimer(r);
        }
    }

    /**
     * Saves the RegenRegion data to a JSON file.
     * @param file The file to save regenerating region data to.
     */
    public void saveRegions(File file) {
        String json = RegenBlocks.GSON.toJson(new ArrayList<>(regions.values()));
        Util.save(file, json, (written) -> {});
    }

    private void startRegenTimer(RegenRegion region) {
        if (regenTasks.containsKey(region.getId()))
            regenTasks.get(region.getId()).cancel();

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> regen(region, false), 20L, plugin.getConfig().getInt("regen-timer"));
        regenTasks.put(region.getId(), task);
    }

    /**
     * Will force a regeneration of specified region.
     * @param region Region to regenerate
     * @param restartTimer Should the timer be restarted
     */
    public void forceRegen(RegenRegion region, boolean restartTimer) {
        if (restartTimer) {
            regenTasks.get(region.getId()).cancel();
            startRegenTimer(region);
        }

        regen(region, true);
    }

    private void regen(RegenRegion region, boolean ignoreTime) {
        for (Location location : new ArrayList<>(region.getRegenerationData().keySet())) {
            RegenData blockData = region.getRegenerationData().get(location);
            if (!ignoreTime && !blockData.getTimeBroken().plus(region.getRegenerationSeconds(), ChronoUnit.SECONDS).isBefore(Instant.now()))
                return;

            // Call the RegenerateEvent event, allow for other plugins to change regeneration data
            RegenerateEvent event = new RegenerateEvent(region, blockData, location);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            blockData = event.getData();
            Block block = region.getWorld().getBlockAt(location);
            if (block.getType() != null && block.getType() != Material.AIR)
                location.getWorld().dropItem(location, new ItemStack(block.getType(), 1)); // Drop the placed block if necessary

            // Place the new block
            block.setType(blockData.getMaterial());
            block.setData(blockData.getBlockDamage());

            region.getRegenerationData().remove(location);
        }
    }

    /**
     * Creates a regenerating region/
     * @param id The region ID
     * @param world The world of the WorldGuard region
     * @param region WorldGuard Region
     * @return The new Regenerating Region {@link RegenRegion}
     */
    public RegenRegion createRegenRegion(String id, World world, ProtectedRegion region) {
        RegenRegion r = new RegenRegion(id.toLowerCase(), world, region);
        regions.put(id.toLowerCase(), r);

        startRegenTimer(r);
        return r;
    }

    public Map<String, RegenRegion> getRegionMap() {
        return Collections.unmodifiableMap(regions);
    }

    public RegenRegion getRegion(String id) {
        return regions.get(id.toLowerCase());
    }

    /**
     * Deletes a region if it exists
     * @param region RegenRegion instance of the region
     */
    public void deleteRegion(RegenRegion region) {
        if (regenTasks.containsKey(region.getId()))
            regenTasks.get(region.getId()).cancel();
        regions.remove(region.getId().toLowerCase());
    }
}
