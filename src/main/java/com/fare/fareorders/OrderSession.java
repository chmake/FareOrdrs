package com.fare.fareorders;

import org.bukkit.Material;

import java.util.UUID;

public final class OrderSession {

    public enum Stage {
        SELECT_ITEM,
        ENTER_AMOUNT,
        ENTER_PRICE,
        CONFIRM
    }

    private final UUID player;

    private Stage stage;
    private Material material;
    private long amount;
    private double price;

    public OrderSession(UUID player) {
        this.player = player;
        this.stage = Stage.SELECT_ITEM;
    }

    public UUID getPlayer() {
        return player;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotalPrice() {
        return amount * price;
    }
}
