package me.schooltests.regenblocks.regions;

import org.bukkit.Location;
import org.bukkit.Material;

import java.time.Instant;

public final class RegenData {
    private transient final Instant timeBroken;
    private final Location location;
    private final Material material;
    private final byte blockDamage;

    public RegenData(Location location, Material material, byte blockDamage) {
        this.timeBroken = Instant.now();
        this.location = location;
        this.material = material;
        this.blockDamage = blockDamage;
    }

    public Material getMaterial() {
        return material;
    }

    public byte getBlockDamage() {
        return blockDamage;
    }

    public Instant getTimeBroken() {
        return timeBroken;
    }

    public Location getLocation() {
        return location;
    }
}