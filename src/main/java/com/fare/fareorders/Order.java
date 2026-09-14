package com.fare.fareorders;

import java.util.UUID;

public final class Order {

    private final long id;
    private final UUID owner;
    private final String item;
    private final long amount;
    private final double price;
    private final long createdAt;
    private final long expiresAt;

    private long remaining;

    public Order(
            long id,
            UUID owner,
            String item,
            long amount,
            double price,
            long createdAt,
            long expiresAt,
            long remaining
    ) {
        this.id = id;
        this.owner = owner;
        this.item = item;
        this.amount = amount;
        this.price = price;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.remaining = remaining;
    }

    public long getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getItem() {
        return item;
    }

    public long getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public long getFulfilled() {
        return amount - remaining;
    }

    public double getRemainingValue() {
        return remaining * price;
    }

    public boolean isComplete() {
        return remaining <= 0;
    }

    public boolean isExpired() {
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt;
    }
}
