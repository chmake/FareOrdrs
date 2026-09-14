package com.fare.fareorders;

import org.bukkit.Material;
import java.util.UUID;

public final class Order {
    private final long id;
    private final UUID owner;
    private final Material material;
    private final long amount;
    private final double price;
    private final long createdAt;
    private final long expiresAt;
    private long remaining;
    private long delivered;

    public Order(long id, UUID owner, Material material, long amount, double price,
                 long createdAt, long expiresAt, long remaining, long delivered) {
        this.id = id;
        this.owner = owner;
        this.material = material;
        this.amount = amount;
        this.price = price;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.remaining = remaining;
        this.delivered = delivered;
    }

    public long getId() { return id; }
    public UUID getOwner() { return owner; }
    public Material getMaterial() { return material; }
    public long getAmount() { return amount; }
    public double getPrice() { return price; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public long getRemaining() { return remaining; }
    public long getDelivered() { return delivered; }
    public long getFulfilled() { return amount - remaining; }
    public double getRemainingValue() { return remaining * price; }
    public double getTotalValue() { return amount * price; }
    public boolean isComplete() { return remaining <= 0; }
    public boolean isExpired() { return expiresAt > 0 && System.currentTimeMillis() >= expiresAt; }
    public void setRemaining(long remaining) { this.remaining = Math.max(0, remaining); }
    public void setDelivered(long delivered) { this.delivered = Math.max(0, delivered); }
    public void addDelivered(long amount) { this.delivered += amount; }
}
