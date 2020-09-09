package me.schooltests.regenblocks.commands;

import me.schooltests.regenblocks.BreakableBlocksProvider;
import me.schooltests.regenblocks.Util;
import me.schooltests.regenblocks.regions.RegenRegion;
import org.bukkit.ChatColor;
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
                    player.sendMessage(ChatColor.GRAY + "Updated setting " + ChatColor.GOLD + setting + ChatColor.GRAY + " to be " + ChatColor.GOLD + Arrays.toString(value.toArray()) + ChatColor.GRAY + "!");
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
            player.sendMessage(ChatColor.GRAY + "Updated setting " + ChatColor.GOLD + setting + ChatColor.GRAY
                    + " to be " + ChatColor.GOLD + stringValue.toUpperCase() + ChatColor.GRAY + "!");
        return true;
    }
}