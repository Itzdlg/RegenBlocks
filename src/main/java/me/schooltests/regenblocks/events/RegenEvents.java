package me.schooltests.regenblocks.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.schooltests.regenblocks.RegenBlocks;
import me.schooltests.regenblocks.Util;
import me.schooltests.regenblocks.events.api.RegenerateEvent;
import me.schooltests.regenblocks.regions.RegenData;
import me.schooltests.regenblocks.regions.RegenRegion;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashSet;
import java.util.Set;

public class RegenEvents implements Listener {
    private final RegenBlocks plugin;
    private int brokenBlocks;

    private final Set<Location> playerPlaced;
    public RegenEvents(RegenBlocks plugin) {
        this.plugin = plugin;
        brokenBlocks = 0;

        this.playerPlaced = new HashSet<>();
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        RegenRegion r = getRegenRegion(event.getBlockPlaced().getLocation());
        if (r == null) return;

        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            event.setCancelled(false);
        else if (r.isAllowPlacing()) {
            playerPlaced.add(event.getBlock().getLocation());
            event.setCancelled(false);
        } else event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(breakBlock(event.getBlock(), event.getPlayer()));
    }

    @EventHandler
    public void onRegenerate(RegenerateEvent event) {
        playerPlaced.remove(event.getRegenData().getLocation());
    }

    private RegenRegion getRegenRegion(Location l) {
        Set<ProtectedRegion> regionsAt = Util.getRegionsAt(l);
        for (RegenRegion r : plugin.getAPI().getRegionMap().values()) {
            if (r.getWorld().getName().equals(l.getWorld().getName()) && r.isGlobal())
                return r;
            else if (r.getRegion() != null && regionsAt.contains(r.getRegion()))
                return r;
        }

        return null;
    }

    private boolean breakBlock(Block b, Player p) {
        if (playerPlaced.contains(b.getLocation())) {
            playerPlaced.remove(b.getLocation());
            return false;
        }

        RegenRegion r = getRegenRegion(b.getLocation());
        if (r == null
                || (!r.isRegenDestroyedByCreative() && p.getGameMode() == GameMode.CREATIVE)) return false;
        if (r.getBreakableBlocks().contains(b.getType())) {
            r.getRegenerationData().add(new RegenData(b.getLocation(), b.getType(), b.getData()));
            brokenBlocks++;

            if (plugin.getConfig().getInt("num-blocks-broken-before-save") != -1 && brokenBlocks >= plugin.getConfig().getInt("num-blocks-broken-before-save")) {
                brokenBlocks = 0;
                plugin.getAPI().saveRegions(RegenBlocks.REGIONS_FILE);
            }

            return false;
        }

        return true;
    }
}