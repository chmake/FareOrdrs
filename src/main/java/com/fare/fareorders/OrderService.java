package com.fare.fareorders;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.sql.SQLException;
import java.util.UUID;

public final class OrderService {
    private final FareOrders plugin;

    public OrderService(FareOrders plugin) { this.plugin = plugin; }

    public Order create(Player buyer, Material material, long amount, double price) throws Exception {
        validate(material, amount, price);
        double total = amount * price;
        if (!Double.isFinite(total) || total <= 0) throw new IllegalArgumentException("Order total is invalid.");
        if (!plugin.getEconomyManager().isAvailable()) throw new IllegalStateException("No economy provider is available.");
        if (!plugin.getEconomyManager().has(buyer, total)) throw new IllegalStateException("You cannot afford this order.");

        if (!plugin.getEconomyManager().withdraw(buyer, total)) throw new IllegalStateException("Could not reserve the order funds.");
        try {
            return plugin.getOrderManager().create(buyer.getUniqueId(), material, amount, price, plugin.getConfig().getLong("orders.expiration-seconds", 86400L) * 1000L);
        } catch (Exception ex) {
            plugin.getEconomyManager().deposit(buyer, total);
            throw ex;
        }
    }

    public long fulfill(Player fulfiller, Order order, long requested) throws Exception {
        if (order == null) throw new IllegalArgumentException("Order not found.");
        if (order.getOwner().equals(fulfiller.getUniqueId())) throw new IllegalArgumentException("You cannot fulfill your own order.");
        expireIfNeeded(order);
        if (order.isExpired()) throw new IllegalStateException("This order has expired.");
        long amount = Math.min(requested, order.getRemaining());
        if (amount <= 0) throw new IllegalStateException("Nothing remains on this order.");
        long available = count(fulfiller, order.getMaterial());
        amount = Math.min(amount, available);
        if (amount <= 0) throw new IllegalStateException("You do not have the requested item.");

        remove(fulfiller, order.getMaterial(), amount);
        try {
            plugin.getOrderManager().fulfill(order, amount);
            double payout = amount * order.getPrice();
            if (!plugin.getEconomyManager().deposit(fulfiller, payout)) {
                // Restore the items if the economy provider rejects the payout.
                give(fulfiller, order.getMaterial(), amount);
                order.setRemaining(order.getRemaining() + amount);
                order.setDelivered(order.getDelivered() - amount);
                plugin.getOrderManager().save(order);
                throw new IllegalStateException("Payout failed; your items were restored.");
            }
            return amount;
        } catch (Exception ex) {
            // The database/economy boundary cannot be made perfectly atomic by Vault.
            throw ex;
        }
    }

    public long claim(Player buyer, Order order) throws SQLException {
        if (order == null || !order.getOwner().equals(buyer.getUniqueId())) return 0;
        long claimable = order.getDelivered();
        if (claimable <= 0) return 0;
        int maxStack = order.getMaterial().getMaxStackSize();
        long capacity = (long) countFreeCapacity(buyer, order.getMaterial(), maxStack);
        long amount = Math.min(claimable, capacity);
        if (amount <= 0) return 0;
        give(buyer, order.getMaterial(), amount);
        order.setDelivered(order.getDelivered() - amount);
        plugin.getOrderManager().save(order);
        return amount;
    }

    public boolean cancel(Player buyer, Order order) throws SQLException {
        if (order == null || !order.getOwner().equals(buyer.getUniqueId())) return false;
        if (order.isComplete()) return false;
        double refund = order.getRemainingValue();
        plugin.getOrderManager().delete(order);
        if (refund > 0) plugin.getEconomyManager().deposit(buyer, refund);
        return true;
    }

    public boolean expireIfNeeded(Order order) throws SQLException {
        if (order == null || !order.isExpired() || order.getRemaining() <= 0) return false;
        double refund = order.getRemainingValue();
        order.setRemaining(0);
        plugin.getOrderManager().save(order);
        if (refund > 0) plugin.getEconomyManager().deposit(plugin.getServer().getOfflinePlayer(order.getOwner()), refund);
        return true;
    }

    private void validate(Material material, long amount, double price) {
        if (material == null || material.isAir()) throw new IllegalArgumentException("Invalid item.");
        if (amount < plugin.getConfig().getLong("orders.min-quantity", 1)) throw new IllegalArgumentException("Quantity is below the minimum.");
        if (amount > plugin.getConfig().getLong("orders.max-quantity", 2304)) throw new IllegalArgumentException("Quantity is above the maximum.");
        if (!Double.isFinite(price) || price < plugin.getConfig().getDouble("orders.min-price", 0.01) || price > plugin.getConfig().getDouble("orders.max-price", 1.0E12)) throw new IllegalArgumentException("Price is outside the allowed range.");
    }

    private long count(Player player, Material material) {
        long total = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) if (stack != null && stack.getType() == material) total += stack.getAmount();
        return total;
    }

    private void remove(Player player, Material material, long amount) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        long left = amount;
        for (int i = 0; i < contents.length && left > 0; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() != material) continue;
            int take = (int)Math.min(left, stack.getAmount());
            stack.setAmount(stack.getAmount() - take);
            left -= take;
            if (stack.getAmount() <= 0) contents[i] = null;
        }
        player.getInventory().setStorageContents(contents);
    }

    private int countFreeCapacity(Player player, Material material, int maxStack) {
        int slots = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack == null || stack.getType().isAir()) slots += maxStack;
            else if (stack.getType() == material) slots += Math.max(0, maxStack - stack.getAmount());
        }
        return slots;
    }

    private void give(Player player, Material material, long amount) {
        long left = amount;
        while (left > 0) {
            int give = (int)Math.min(left, material.getMaxStackSize());
            for (ItemStack leftover : player.getInventory().addItem(new ItemStack(material, give)).values()) player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            left -= give;
        }
    }
}
