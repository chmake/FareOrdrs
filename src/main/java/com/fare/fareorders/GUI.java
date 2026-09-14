package com.fare.fareorders;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class GUI {

    private final FareOrders plugin;

    public GUI(FareOrders plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(
                null,
                54,
                ChatColor.DARK_GRAY + "FareOrders"
        );

        fillBackground(inventory);

        inventory.setItem(20, createItem(
                Material.CHEST,
                ChatColor.GOLD + "Browse Orders",
                ChatColor.GRAY + "View active player orders."
        ));

        inventory.setItem(22, createItem(
                Material.EMERALD,
                ChatColor.GREEN + "Create Order",
                ChatColor.GRAY + "Request items from other players."
        ));

        inventory.setItem(24, createItem(
                Material.BOOK,
                ChatColor.YELLOW + "My Orders",
                ChatColor.GRAY + "View your active orders."
        ));

        inventory.setItem(49, createItem(
                Material.BARRIER,
                ChatColor.RED + "Close",
                ChatColor.GRAY + "Close FareOrders."
        ));

        player.openInventory(inventory);
    }

    public void openCreateOrder(Player player) {
        Inventory inventory = Bukkit.createInventory(
                null,
                54,
                ChatColor.DARK_GREEN + "Create Order"
        );

        fillBackground(inventory);

        inventory.setItem(20, createItem(
                Material.DIAMOND,
                ChatColor.AQUA + "Select Item",
                ChatColor.GRAY + "Choose the item you want."
        ));

        inventory.setItem(22, createItem(
                Material.PAPER,
                ChatColor.YELLOW + "Quantity",
                ChatColor.GRAY + "Set how many items you need."
        ));

        inventory.setItem(24, createItem(
                Material.EMERALD,
                ChatColor.GREEN + "Price",
                ChatColor.GRAY + "Set the price per item."
        ));

        inventory.setItem(40, createItem(
                Material.LIME_DYE,
                ChatColor.GREEN + "Confirm Order",
                ChatColor.GRAY + "Create the order."
        ));

        inventory.setItem(49, createItem(
                Material.BARRIER,
                ChatColor.RED + "Cancel",
                ChatColor.GRAY + "Cancel order creation."
        ));

        player.openInventory(inventory);
    }

    private void fillBackground(Inventory inventory) {
        ItemStack filler = createItem(
                Material.GRAY_STAINED_GLASS_PANE,
                " ",
                ""
        );

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private ItemStack createItem(
            Material material,
            String name,
            String... lore
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(name);

        if (lore.length > 0) {
            List<String> loreLines = new ArrayList<>();

            for (String line : lore) {
                loreLines.add(line);
            }

            meta.setLore(loreLines);
        }

        item.setItemMeta(meta);
        return item;
    }
}
