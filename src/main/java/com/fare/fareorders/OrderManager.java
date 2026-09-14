package com.fare.fareorders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class OrderManager {

    private final AtomicLong nextId = new AtomicLong(1);
    private final List<Order> orders = new ArrayList<>();

    public synchronized Order createOrder(
            UUID owner,
            String item,
            long amount,
            double price,
            long expiration
    ) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null.");
        }

        if (item == null || item.isBlank()) {
            throw new IllegalArgumentException("Item cannot be empty.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }

        long now = System.currentTimeMillis();
        long expiresAt = expiration <= 0 ? 0 : now + expiration;

        Order order = new Order(
                nextId.getAndIncrement(),
                owner,
                item,
                amount,
                price,
                now,
                expiresAt,
                amount
        );

        orders.add(order);

        return order;
    }

    public synchronized Order getOrder(long id) {
        for (Order order : orders) {
            if (order.getId() == id) {
                return order;
            }
        }

        return null;
    }

    public synchronized List<Order> getOrders() {
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }

    public synchronized List<Order> getActiveOrders() {
        List<Order> result = new ArrayList<>();

        for (Order order : orders) {
            if (!order.isComplete() && !order.isExpired()) {
                result.add(order);
            }
        }

        return result;
    }

    public synchronized List<Order> getOrders(UUID owner) {
        List<Order> result = new ArrayList<>();

        for (Order order : orders) {
            if (order.getOwner().equals(owner)) {
                result.add(order);
            }
        }

        return result;
    }

    public synchronized boolean cancelOrder(long id, UUID owner) {
        Order order = getOrder(id);

        if (order == null) {
            return false;
        }

        if (!order.getOwner().equals(owner)) {
            return false;
        }

        return orders.remove(order);
    }

    public synchronized long fulfill(Order order, long amount) {
        if (order == null || amount <= 0) {
            return 0;
        }

        if (order.isComplete() || order.isExpired()) {
            return 0;
        }

        long fulfilled = Math.min(amount, order.getRemaining());

        order.setRemaining(order.getRemaining() - fulfilled);

        return fulfilled;
    }

    public synchronized long getPlayerOrderCount(UUID owner) {
        long count = 0;

        for (Order order : orders) {
            if (order.getOwner().equals(owner) && !order.isComplete()) {
                count++;
            }
        }

        return count;
    }

    public synchronized void removeExpiredOrders() {
        orders.removeIf(Order::isExpired);
    }

    public synchronized long getNextId() {
        return nextId.get();
    }

    public synchronized void setNextId(long nextId) {
        this.nextId.set(nextId);
    }
}
