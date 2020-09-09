package me.schooltests.regenblocks.events;

import me.schooltests.regenblocks.RegenBlocks;
import me.schooltests.regenblocks.Util;
import me.schooltests.regenblocks.regions.RegenRegion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkEvents implements Listener {
    private final RegenBlocks plugin;

    public ChunkEvents(RegenBlocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void chunkUnload(ChunkUnloadEvent event) {
        if (!plugin.getConfig().getBoolean("deny-unload")) return;
        for (RegenRegion r : plugin.getAPI().getRegionMap().values()) {
            if (r.getRegion() == null && !r.isGlobal()) continue;
            if (r.isGlobal() || Util.regionExistsAtChunk(event.getChunk(), r.getRegion()))
                event.setCancelled(true);
        }
    }
}