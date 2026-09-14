package com.fare.fareorders;

import org.bukkit.Material;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public final class OrderManager {
    private final FareOrders plugin;
    private final Map<Long, Order> orders = new LinkedHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public OrderManager(FareOrders plugin) { this.plugin = plugin; }

    public synchronized void load() throws SQLException {
        orders.clear();
        for (Order order : plugin.getDatabase().getAllOrders()) orders.put(order.getId(), order);
        nextId.set(Math.max(1, plugin.getDatabase().maxId() + 1));
    }

    public synchronized Order get(long id) { return orders.get(id); }

    public synchronized List<Order> active() {
        List<Order> result = new ArrayList<>();
        for (Order order : orders.values()) if (!order.isComplete() && !order.isExpired()) result.add(order);
        return result;
    }

    public synchronized List<Order> byOwner(UUID owner) {
        List<Order> result = new ArrayList<>();
        for (Order order : orders.values()) if (order.getOwner().equals(owner)) result.add(order);
        return result;
    }

    public synchronized long activeCount(UUID owner) {
        return byOwner(owner).stream().filter(o -> !o.isComplete() && !o.isExpired()).count();
    }

    public synchronized Order create(UUID owner, Material material, long amount, double price, long expirationMs) throws SQLException {
        long id = nextId.getAndIncrement();
        long now = System.currentTimeMillis();
        long expires = expirationMs <= 0 ? 0 : now + expirationMs;
        Order order = new Order(id, owner, material, amount, price, now, expires, amount, 0);
        plugin.getDatabase().insertOrder(order);
        orders.put(id, order);
        return order;
    }

    public synchronized long fulfill(Order order, long amount) throws SQLException {
        if (order == null || amount <= 0 || order.isComplete() || order.isExpired()) return 0;
        long delivered = Math.min(amount, order.getRemaining());
        order.setRemaining(order.getRemaining() - delivered);
        order.addDelivered(delivered);
        plugin.getDatabase().updateOrder(order);
        return delivered;
    }

    public synchronized void save(Order order) throws SQLException { plugin.getDatabase().updateOrder(order); }

    public synchronized void delete(Order order) throws SQLException {
        if (order == null) return;
        plugin.getDatabase().deleteOrder(order.getId());
        orders.remove(order.getId());
    }
}
