package me.schooltests.regenblocks.commands;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum RegionSetting {
    REGENERATION_SECONDS("regenerationSeconds"),
    ALLOW_PLACING("allowPlacing"),
    BREAKABLE_BLOCKS("breakableBlocks"),
    REGEN_DESTROYED_BY_CREATIVE("regenDestroyedByCreative");

    public final String display;
    RegionSetting(String display) {
        this.display = display;
    }

    public static List<String> displayValues() {
        return Arrays.stream(values()).map(s -> s.display).collect(Collectors.toList());
    }

    public static RegionSetting match(String name) {
        return Arrays.stream(RegionSetting.values())
                .sorted(Comparator.comparingInt(r -> r.display.length()))
                .filter(r -> r.display.toLowerCase().startsWith(name.toLowerCase()))
                .findFirst()
                .orElse(null);
    }
}
