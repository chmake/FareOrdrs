package com.fare.fareorders;

import org.bukkit.Material;
import java.util.UUID;

public final class OrderSession {
    public enum Stage { SELECT_ITEM, ENTER_AMOUNT, ENTER_PRICE, CONFIRM }
    public enum Input { NONE, SEARCH, AMOUNT, PRICE }

    private final UUID player;
    private Stage stage = Stage.SELECT_ITEM;
    private Input input = Input.NONE;
    private Material material;
    private long amount;
    private double price;
    private int itemPage;
    private String filter = "";

    public OrderSession(UUID player) { this.player = player; }
    public UUID getPlayer() { return player; }
    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
    public Input getInput() { return input; }
    public void setInput(Input input) { this.input = input; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getItemPage() { return itemPage; }
    public void setItemPage(int itemPage) { this.itemPage = Math.max(0, itemPage); }
    public String getFilter() { return filter; }
    public void setFilter(String filter) { this.filter = filter == null ? "" : filter; }
    public double getTotalPrice() { return amount * price; }
}
