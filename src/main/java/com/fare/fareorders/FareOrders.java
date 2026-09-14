package com.fare.fareorders;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FareOrders extends JavaPlugin {

    private static FareOrders instance;

    private OrderManager orderManager;
    private Database database;
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        orderManager = new OrderManager();
        database = new Database(this);
        economyManager = new EconomyManager(this);

        try {
            database.connect();
        } catch (Exception exception) {
            getLogger().severe("Failed to connect to SQLite.");
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!economyManager.setup()) {
            getLogger().warning("No economy provider detected.");
            getLogger().warning("Money-related features will be unavailable.");
        }

        PluginCommand ordersCommand = getCommand("orders");

        if (ordersCommand != null) {
            ordersCommand.setExecutor(new OrdersCommand(this));
        }

        getLogger().info("FareOrders enabled.");
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }

        instance = null;

        getLogger().info("FareOrders disabled.");
    }

    public static FareOrders getInstance() {
        return instance;
    }

    public OrderManager getOrderManager() {
        return orderManager;
    }

    public Database getDatabase() {
        return database;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}
