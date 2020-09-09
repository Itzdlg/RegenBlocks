package me.schooltests.regenblocks.regions;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class RegenRegion {
    private final String id;
    private final String world;
    private final String region;
    private int regenerationSeconds;
    private boolean allowPlacing;
    private List<String> breakableBlocks;
    private boolean regenDestroyedByCreative;

    private Map<Location, RegenData> regenerationData;

    public RegenRegion(String id, World world, ProtectedRegion region) {
        this.id = id.toLowerCase();
        this.world = world.getName();
        this.region = region.getId();

        this.regenerationSeconds = 30;
        this.allowPlacing = false;
        this.breakableBlocks = new ArrayList<>();
        this.regenDestroyedByCreative = false;
        this.regenerationData = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    public ProtectedRegion getRegion() {
        if (getWorld() == null) return null;
        return WorldGuardPlugin.inst().getRegionManager(getWorld()).getRegion(region);
    }

    public boolean isGlobal() {
        return region.equalsIgnoreCase("__global__");
    }

    public int getRegenerationSeconds() {
        return regenerationSeconds;
    }

    public void setRegenerationSeconds(int regenerationSeconds) {
        this.regenerationSeconds = regenerationSeconds;
    }

    public boolean isAllowPlacing() {
        return allowPlacing;
    }

    public void setAllowPlacing(boolean allowPlacing) {
        this.allowPlacing = allowPlacing;
    }

    public List<Material> getBreakableBlocks() {
        return breakableBlocks.stream().map(Material::matchMaterial).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void setBreakableBlocks(List<Material> breakableBlocks) {
        this.breakableBlocks = breakableBlocks.stream().map(Enum::name).collect(Collectors.toList());
    }

    public boolean isRegenDestroyedByCreative() {
        return regenDestroyedByCreative;
    }

    public void setRegenDestroyedByCreative(boolean regenDestroyedByCreative) {
        this.regenDestroyedByCreative = regenDestroyedByCreative;
    }

    public Map<Location, RegenData> getRegenerationData() {
        return regenerationData;
    }
}
