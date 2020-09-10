package me.schooltests.regenblocks.commands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.schooltests.regenblocks.RegenBlocks;
import me.schooltests.regenblocks.RegenBlocksAPI;
import me.schooltests.regenblocks.Util;
import me.schooltests.regenblocks.regions.RegenRegion;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;

public class RegenBlocksCommand implements CommandExecutor {
    private final RegenBlocks plugin;
    private final RegenBlocksAPI API;

    private final EditCommand editCommand = new EditCommand();
    private final String usage;
    public RegenBlocksCommand(RegenBlocks plugin) {
        this.plugin = plugin;
        this.API = plugin.getAPI();

        usage = Util.color("&7Command: &6/rblocks\n&7%s" +
                "\n&7/rblocks &6create <name> <region>" +
                "\n&7/rblocks &6delete <name>" +
                "\n&7/rblocks &6edit <name> <setting> <value>" +
                "\n&7/rblocks &6info [name]" +
                "\n&7%{0}", ChatColor.STRIKETHROUGH + repeat("-", 27));

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(usage);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may use this command!");
            return true;
        }

        Player player = (Player) sender;
        switch (args[0].toLowerCase()) { // Sub command
            case "new":
            case "add":
            case "create": {
                if (args.length < 3) {
                    player.sendMessage(usage);
                    return true;
                }

                ProtectedRegion pr = toWorldGuardRegion(args[2], player.getWorld());
                if (pr == null) {
                    player.sendMessage(usage);
                    return true;
                }

                if (API.getRegionMap().containsKey(args[1].toLowerCase())) {
                    player.sendMessage(ChatColor.RED + "There is already a regenerating region with that name!");
                    return true;
                }

                if (API.getRegionMap().values().stream()
                        .filter(r -> r.getRegion() != null
                                || r.isGlobal()).anyMatch(r -> (r.isGlobal() && pr.getId().equalsIgnoreCase("__global__"))
                                || r.getRegion().getId().equalsIgnoreCase(pr.getId()))) {
                    player.sendMessage(ChatColor.RED + "There is already a regenerating region with that worldguard region!");
                    return true;
                }

                RegenRegion r = API.createRegenRegion(args[1], player.getWorld(), pr);
                player.sendMessage(Util.color("&7Successfully created a new regenerating region named &6%s &7in worldguard region &6%s&7!", r.getId(), pr.getId()));
                break;
            }
            case "del":
            case "rem":
            case "remove":
            case "delete": {
                if (args.length < 2) {
                    player.sendMessage(usage);
                    return true;
                }

                RegenRegion r = toRegenRegion(args[1]);
                if (r == null) {
                    player.sendMessage(ChatColor.RED + "There is no regenerating region with that name!");
                    return true;
                }

                API.deleteRegion(r);
                player.sendMessage(Util.color("&7Deleted regenerating region named &6%s&7!", r.getId()));
                break;
            }
            case "e":
            case "edit": {
                if (args.length < 3) {
                    player.sendMessage(usage);
                    return true;
                }

                RegenRegion region = toRegenRegion(args[1]);
                if (region == null) { // Does the region given exist?
                    player.sendMessage(usage);
                    return true;
                }

                RegionSetting setting = RegionSetting.match(args[2]);
                if (setting == null) {
                    player.sendMessage(Util.color("&7Modifiable settings: &6%s", String.join(", ", RegionSetting.displayValues())));
                    return true;
                } else if (setting != RegionSetting.BREAKABLE_BLOCKS && args.length < 4) {
                    player.sendMessage(usage);
                    return true;
                }

                String stringValue = setting != RegionSetting.BREAKABLE_BLOCKS ? args[3] : "";
                boolean success = editCommand.execute(player, region, setting, stringValue);
                if (!success) player.sendMessage(Util.color("&7Modifiable settings: &6%s", String.join(", ", RegionSetting.displayValues())));
                break;
            }
            case "list":
            case "info":
                if (args.length >= 2) { // Ex. info forest
                    RegenRegion r = toRegenRegion(args[1]);
                    if (r == null) {
                        player.sendMessage(ChatColor.RED + "There is no regenerating region with that name!");
                        return true;
                    }

                    String region = "";
                    if (r.getRegion() == null && !r.isGlobal()) region = "NONE";
                    else if (r.getRegion() == null && r.isGlobal()) region = "__global__";
                    else if (r.getRegion() != null) region = r.getRegion().getId();

                    String msg = "&7Settings for &6%s&7:" +
                            "\n&7Regeneration Seconds: &6%s" +
                            "\n&7World: &6%s" +
                            "\n&7WorldGuard Region: &6%s" +
                            "\n&7Regen Blocks Destroyed By Creative: &6%s" +
                            "\n&7Allow Placing: &6%s";
                    player.sendMessage(Util.color(msg, r.getId(), r.getRegenerationSeconds(), r.getWorld().getName(),
                            region, r.isRegenDestroyedByCreative(), r.isAllowPlacing()));
                } else if (API.getRegionMap().size() == 0) {
                    player.sendMessage(ChatColor.GRAY + "There are no regenerating regions!");
                } else { // Prints a list of regenerating regions
                     StringBuilder sb = new StringBuilder();
                     sb.append(ChatColor.GRAY).append("Regenerating Regions: ");
                     for (RegenRegion r : API.getRegionMap().values())
                         sb.append("\n").append(ChatColor.GRAY).append("- ").append(ChatColor.GOLD).append(r.getId());

                     player.sendMessage(sb.toString());
                }
                break;
            default:
                player.sendMessage(usage);
        }

        return true;
    }

    private String repeat(String s, int i) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x <= i; x++)
            sb.append(s);
        return sb.toString();
    }

    private RegenRegion toRegenRegion(String s) {
        return API.getRegionMap().keySet().stream()
                .sorted(Comparator.comparing(String::length))
                .filter(key -> key.startsWith(s.toLowerCase()))
                .map(API::getRegion)
                .findFirst()
                .orElse(null);
    }

    private ProtectedRegion toWorldGuardRegion(String s, World w) {
        return WorldGuardPlugin.inst().getRegionManager(w).getRegions().keySet().stream()
                .sorted(Comparator.comparing(String::length))
                .filter(rName -> rName.startsWith(s))
                .map(rName -> WorldGuardPlugin.inst().getRegionManager(w).getRegion(rName))
                .findFirst()
                .orElse(null);
    }
}
