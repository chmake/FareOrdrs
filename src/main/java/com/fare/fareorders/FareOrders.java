package com.fare.fareorders;

import org.bukkit.plugin.java.JavaPlugin;

public final class FareOrders extends JavaPlugin {

    private static FareOrders instance;
    private OrderManager orderManager;

    @Override
    public void onEnable() {
        instance = this;

        orderManager = new OrderManager();

        if (getCommand("orders") != null) {
            getCommand("orders").setExecutor(new OrdersCommand(this));
        }

        getLogger().info("FareOrders enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("FareOrders disabled.");
        instance = null;
    }

    public static FareOrders getInstance() {
        return instance;
    }

    public OrderManager getOrderManager() {
        return orderManager;
    }
}
