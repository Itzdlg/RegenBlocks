package me.schooltests.regenblocks.commands;

import me.schooltests.regenblocks.BreakableBlocksProvider;
import me.schooltests.regenblocks.Util;
import me.schooltests.regenblocks.regions.RegenRegion;
import org.bukkit.entity.Player;

import java.util.Arrays;
class EditCommand {
    boolean execute(Player player, RegenRegion region, RegionSetting setting, String stringValue) {
        switch (setting) {
            case REGENERATION_SECONDS: {
                Integer value = Util.toInt(stringValue);
                if (value == null)
                    return false;
                region.setRegenerationSeconds(value);
                break;
            }
            case ALLOW_PLACING: {
                Boolean value = Util.toBool(stringValue);
                if (value == null) return false;
                region.setAllowPlacing(value);
                break;
            }
            case BREAKABLE_BLOCKS: {
                BreakableBlocksProvider.prompt(player, region, (value) -> {
                    region.setBreakableBlocks(value);
                    player.sendMessage(Util.color("&7Updated setting &6%s&7 to be &6%s&7!", setting, Arrays.toString(value.toArray())));
                });
                break;
            }
            case REGEN_DESTROYED_BY_CREATIVE: {
                Boolean value = Util.toBool(stringValue);
                if (value == null) return false;
                region.setRegenDestroyedByCreative(value);
                break;
            }
            default:
                return false;
        }

        if (!stringValue.isEmpty())
            player.sendMessage(Util.color("&7Updated setting &6%s&7 to be &6%s&7!", setting, stringValue.toUpperCase()));
        return true;
    }
}