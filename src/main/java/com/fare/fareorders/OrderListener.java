package com.fare.fareorders;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

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

        if (!title.equals(ChatColor.DARK_GRAY + "FareOrders")
                && !title.equals(ChatColor.DARK_GREEN + "Create Order")) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() < 0
                || event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        if (title.equals(ChatColor.DARK_GRAY + "FareOrders")) {
            handleMainMenu(player, event.getRawSlot());
            return;
        }

        handleCreateOrder(player, event.getRawSlot());
    }

    private void handleMainMenu(Player player, int slot) {
        switch (slot) {
            case 20 -> player.sendMessage(
                    ChatColor.GOLD + "FareOrders » " +
                    ChatColor.GRAY + "Order browsing will be added next."
            );

            case 22 -> {
                plugin.createOrderSession(player.getUniqueId());
                plugin.getGui().openCreateOrder(player);
            }

            case 24 -> player.sendMessage(
                    ChatColor.YELLOW + "FareOrders » " +
                    ChatColor.GRAY + "Your orders will be added next."
            );

            case 49 -> player.closeInventory();

            default -> {
            }
        }
    }

    private void handleCreateOrder(Player player, int slot) {
        OrderSession session = plugin.getOrderSession(player.getUniqueId());

        if (session == null) {
            session = plugin.createOrderSession(player.getUniqueId());
        }

        switch (slot) {
            case 20 -> player.sendMessage(
                    ChatColor.AQUA + "FareOrders » " +
                    ChatColor.GRAY + "Item selection will be added next."
            );

            case 22 -> player.sendMessage(
                    ChatColor.YELLOW + "FareOrders » " +
                    ChatColor.GRAY + "Quantity input will be added next."
            );

            case 24 -> player.sendMessage(
                    ChatColor.GREEN + "FareOrders » " +
                    ChatColor.GRAY + "Price input will be added next."
            );

            case 40 -> player.sendMessage(
                    ChatColor.GREEN + "FareOrders » " +
                    ChatColor.GRAY + "The order cannot be confirmed yet."
            );

            case 49 -> {
                plugin.removeOrderSession(player.getUniqueId());
                player.closeInventory();
            }

            default -> {
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.DARK_GRAY + "FareOrders")
                || title.equals(ChatColor.DARK_GREEN + "Create Order")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();

        if (!title.equals(ChatColor.DARK_GREEN + "Create Order")) {
            return;
        }

        plugin.removeOrderSession(event.getPlayer().getUniqueId());
    }
}
