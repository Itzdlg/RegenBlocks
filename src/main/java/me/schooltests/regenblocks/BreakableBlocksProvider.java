package me.schooltests.regenblocks;

import me.schooltests.regenblocks.regions.RegenRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class BreakableBlocksProvider implements Listener {
    private static final Map<UUID, Consumer<List<Material>>> editing = new HashMap<>();
    public static void prompt(Player player, RegenRegion region, Consumer<List<Material>> handle) {
        editing.put(player.getUniqueId(), handle);
        Inventory inv = Bukkit.createInventory(null, 4 * 9, Util.color("&7Setting: &6Breakable Blocks"));

        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1);
        filler.setDurability((short) 7);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(ChatColor.BLACK.toString());
        filler.setItemMeta(meta);

        ItemStack item = new ItemStack(Material.WOOL, 1);
        item.setDurability((short) 13);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "SAVE");
        item.setItemMeta(meta);

        inv.setItem(7, item);

        item = new ItemStack(Material.WOOL, 1);
        item.setDurability((short) 14);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "CANCEL");
        item.setItemMeta(meta);

        inv.setItem(8, item);

        for (int i = 0; i < 7; i++)
            inv.setItem(i, filler);

        for (Material m : region.getBreakableBlocks())
            inv.addItem(new ItemStack(m, 1));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (Util.invalidInventoryClick(event)) return;
        if (editing.containsKey(event.getWhoClicked().getUniqueId())
                && event.getClickedInventory().getName().equals(Util.color("&7Setting: &6Breakable Blocks"))) {
            if (event.getSlot() <= 6) event.setCancelled(true);
            if (event.getSlot() == 7) {
                List<Material> m = new ArrayList<>();
                for (int i = 9; i <= 35; i++) {
                    ItemStack item = event.getClickedInventory().getItem(i);
                    if (item == null || item.getType() == Material.AIR) continue;
                    if (!m.contains(item.getType())) m.add(item.getType());
                }

                editing.get(event.getWhoClicked().getUniqueId()).accept(m);
                editing.remove(event.getWhoClicked().getUniqueId());
                event.getWhoClicked().closeInventory();
            } else if (event.getSlot() == 8) {
                editing.remove(event.getWhoClicked().getUniqueId());
                event.getWhoClicked().closeInventory();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        editing.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        editing.remove(event.getPlayer().getUniqueId());
    }
}
