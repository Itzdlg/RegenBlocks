package me.schooltests.regenblocks.regions;

import org.bukkit.Material;

import java.time.Instant;

public final class RegenData {
    private transient final Instant timeBroken;
    private final Material material;
    private final byte blockDamage;

    public RegenData(Material material, byte blockDamage) {
        this.timeBroken = Instant.now();
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
}