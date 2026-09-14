package com.fare.fareorders;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public final class OrderListener implements Listener {

    private final FareOrders plugin;

    public OrderListener(FareOrders plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();

        if (!title.equals(ChatColor.DARK_GRAY + "FareOrders")) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() < 0 ||
                event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        switch (event.getRawSlot()) {
            case 20 -> {
                player.sendMessage(
                        ChatColor.GOLD + "FareOrders » " +
                        ChatColor.GRAY + "Order browsing will be added next."
                );
            }

            case 22 -> {
                player.sendMessage(
                        ChatColor.GREEN + "FareOrders » " +
                        ChatColor.GRAY + "Order creation will be added next."
                );
            }

            case 24 -> {
                player.sendMessage(
                        ChatColor.YELLOW + "FareOrders » " +
                        ChatColor.GRAY + "Your orders will be added next."
                );
            }

            case 49 -> player.closeInventory();

            default -> {
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_GRAY + "FareOrders")) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_GRAY + "FareOrders")) {
            return;
        }

        // Reserved for GUI/session cleanup.
        // No polling or repeating task is required.
    }
}
