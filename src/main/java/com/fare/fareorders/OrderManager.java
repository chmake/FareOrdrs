package com.fare.fareorders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class OrderManager {

    private final AtomicLong nextId = new AtomicLong(1);
    private final List<Order> orders = new ArrayList<>();

    public Order createOrder(
            UUID owner,
            String item,
            long amount,
            double price
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

        Order order = new Order(
                nextId.getAndIncrement(),
                owner,
                item,
                amount,
                price
        );

        orders.add(order);
        return order;
    }

    public Order getOrder(long id) {
        for (Order order : orders) {
            if (order.getId() == id) {
                return order;
            }
        }

        return null;
    }

    public boolean removeOrder(long id) {
        return orders.removeIf(order -> order.getId() == id);
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public List<Order> getOrders(UUID owner) {
        List<Order> result = new ArrayList<>();

        for (Order order : orders) {
            if (order.getOwner().equals(owner)) {
                result.add(order);
            }
        }

        return result;
    }

    public long getNextId() {
        return nextId.get();
    }
}
