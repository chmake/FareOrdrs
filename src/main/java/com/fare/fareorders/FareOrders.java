package com.fare.fareorders;

import org.bukkit.plugin.java.JavaPlugin;

public final class FareOrders extends JavaPlugin {

    private static FareOrders instance;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("FareOrders enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("FareOrders disabled.");
    }

    public static FareOrders getInstance() {
        return instance;
    }
}
