package com.fare.fareorders;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FareOrders extends JavaPlugin {

    private static FareOrders instance;

    private OrderManager orderManager;
    private Database database;
    private EconomyManager economyManager;
    private GUI gui;
    private final Map<UUID, OrderSession> orderSessions = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        orderManager = new OrderManager();
        database = new Database(this);
        economyManager = new EconomyManager(this);
        gui = new GUI(this);

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

        getServer().getPluginManager().registerEvents(
                new OrderListener(this),
                this
        );

        getLogger().info("FareOrders enabled.");
    }

    @Override
    public void onDisable() {
        orderSessions.clear();

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

    public GUI getGui() {
        return gui;
    }

    public OrderSession getOrderSession(UUID uuid) {
        return orderSessions.get(uuid);
    }

    public OrderSession createOrderSession(UUID uuid) {
        OrderSession session = new OrderSession(uuid);
        orderSessions.put(uuid, session);
        return session;
    }

    public void removeOrderSession(UUID uuid) {
        orderSessions.remove(uuid);
    }
}
