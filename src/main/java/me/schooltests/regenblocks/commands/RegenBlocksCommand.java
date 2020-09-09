package me.schooltests.regenblocks.commands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.schooltests.regenblocks.RegenBlocks;
import me.schooltests.regenblocks.RegenBlocksAPI;
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

        usage = ChatColor.GRAY + "Command: " + ChatColor.GOLD + "/rblocks"
                + "\n" + ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + repeat("-", 27)
                + "\n" + ChatColor.translateAlternateColorCodes('&',
                    "&7/rblocks &6create <name> <region>"
                    + "\n&7/rblocks &6delete <name>"
                    + "\n&7/rblocks &6edit <name> <setting> <value>"
                    + "\n&7/rblocks &6info [name]"
                ) + "\n" + ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + repeat("-", 27);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
            sender.sendMessage(usage);
        else if (sender instanceof Player) {
            Player player = (Player) sender;
            switch (args[0].toLowerCase()) { // Sub command
                case "new":
                case "add":
                case "create": {
                    if (args.length >= 3) { // Ex. create forest __global__
                        ProtectedRegion pr = toWorldGuardRegion(args[2], player.getWorld());
                        if (pr == null)
                            player.sendMessage(usage);
                        else {
                            if (API.getRegionMap().containsKey(args[1].toLowerCase())) // Is there already a regenerating region with that id?
                                player.sendMessage(ChatColor.RED + "There is already a regenerating region with that name!");
                            // Is there a global regenerating region in that world OR an existing regenerating region with that worldguard region?
                            else if (API.getRegionMap().values().stream().filter(r -> r.getRegion() != null || r.isGlobal()).anyMatch(r -> (r.isGlobal() && pr.getId().equalsIgnoreCase("__global__")) || r.getRegion().getId().equalsIgnoreCase(pr.getId())))
                                player.sendMessage(ChatColor.RED + "There is already a regenerating region with that worldguard region!");
                            else {
                                RegenRegion r = API.createRegenRegion(args[1], player.getWorld(), pr);
                                player.sendMessage(ChatColor.GRAY + "Successfully created a new regenerating region named "
                                        + ChatColor.GOLD + r.getId() + ChatColor.GRAY
                                        + " in worldguard region " + ChatColor.GOLD + pr.getId()
                                        + ChatColor.GRAY + "!");
                            }
                        }
                    } else player.sendMessage(usage);
                    break;
                }
                case "del":
                case "rem":
                case "remove":
                case "delete": {
                    if (args.length >= 2) { // Ex. delete forest
                        RegenRegion r = toRegenRegion(args[1]);
                        if (r == null)
                            player.sendMessage(ChatColor.RED + "There is no regenerating region with that name!");
                        else {
                            API.deleteRegion(r);
                            player.sendMessage(ChatColor.GRAY + "Deleted regenerating region named " + ChatColor.GOLD + r.getId() + ChatColor.GRAY + "!");
                        }
                    } else player.sendMessage(usage);
                    break;
                }
                case "e":
                case "edit": {
                    if (args.length >= 3) { // Ex. edit forest regen 30 (Matches to regenerationSeconds)
                        RegenRegion region = toRegenRegion(args[1]);
                        RegionSetting setting = RegionSetting.match(args[2]);
                        if (region == null) // Does the region given exist?
                            player.sendMessage(usage);
                        else if (setting == null) // Does the setting given match any modifiable setting?
                            player.sendMessage(ChatColor.GRAY + "Modifiable settings: " + ChatColor.GOLD
                                    + String.join(", ", RegionSetting.displayValues()));
                        else if (setting != RegionSetting.BREAKABLE_BLOCKS && args.length < 4) // Does the setting require a value in the args?
                            player.sendMessage(usage);
                        else {
                            boolean success = editCommand.execute(player, region, setting, setting != RegionSetting.BREAKABLE_BLOCKS ? args[3] : "");
                            if (!success) player.sendMessage(ChatColor.GRAY + "Modifiable settings: " + ChatColor.GOLD
                                    + String.join(", ", RegionSetting.displayValues()));
                        }
                    } else player.sendMessage(usage);
                    break;
                }
                case "list":
                case "info":
                    if (args.length >= 2) { // Ex. info forest
                        RegenRegion r = toRegenRegion(args[1]);
                        if (r == null)
                            player.sendMessage(ChatColor.RED + "There is no regenerating region with that name!");
                        else {
                            String region = "";
                            if (r.getRegion() == null && !r.isGlobal()) region = "NONE";
                            else if (r.getRegion() == null && r.isGlobal()) region = "__global__";
                            else if (r.getRegion() != null) region = r.getRegion().getId();
                            String msg = ChatColor.GRAY +
                                    "Settings for " +
                                    ChatColor.GOLD + r.getId() +
                                    ChatColor.GRAY + ": " +
                                    "\n" + ChatColor.GRAY + "Regeneration Seconds: " + ChatColor.GOLD + r.getRegenerationSeconds() +
                                    "\n" + ChatColor.GRAY + "World: " + ChatColor.GOLD + r.getWorld().getName() +
                                    "\n" + ChatColor.GRAY + "WorldGuard Region: " + ChatColor.GOLD + region +
                                    "\n" + ChatColor.GRAY + "Regen Blocks Destroyed By Creative: " + ChatColor.GOLD + r.isRegenDestroyedByCreative() +
                                    "\n" + ChatColor.GRAY + "Allow Placing: " + ChatColor.GOLD + r.isAllowPlacing();
                            player.sendMessage(msg);
                        }
                    } else if (API.getRegionMap().size() == 0) // Ex. info
                        player.sendMessage(ChatColor.GRAY + "There are no regenerating regions!");
                     else { // Prints a list of regenerating regions
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
        } else sender.sendMessage(ChatColor.GRAY + "Only players may use this command!");
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
