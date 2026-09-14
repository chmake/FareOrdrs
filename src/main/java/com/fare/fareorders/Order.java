package com.fare.fareorders;

import java.util.UUID;

public final class Order {

    private final long id;
    private final UUID owner;
    private final String item;
    private final long amount;
    private final double price;
    private long remaining;

    public Order(
            long id,
            UUID owner,
            String item,
            long amount,
            double price
    ) {
        this.id = id;
        this.owner = owner;
        this.item = item;
        this.amount = amount;
        this.price = price;
        this.remaining = amount;
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

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public boolean isComplete() {
        return remaining <= 0;
    }
}
