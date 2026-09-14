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
    private OrderService orderService;
    private GUI gui;
    private final Map<UUID, OrderSession> orderSessions = new HashMap<>();

    @Override public void onEnable() {
        instance = this;
        saveDefaultConfig();
        orderManager = new OrderManager(this);
        database = new Database(this);
        economyManager = new EconomyManager(this);
        orderService = new OrderService(this);
        gui = new GUI(this);
        try { database.connect(); orderManager.load(); }
        catch (Exception e) { getLogger().severe("Failed to initialize SQLite: " + e.getMessage()); e.printStackTrace(); getServer().getPluginManager().disablePlugin(this); return; }
        economyManager.setup();
        PluginCommand command = getCommand("orders");
        if (command != null) command.setExecutor(new OrdersCommand(this));
        getServer().getPluginManager().registerEvents(new OrderListener(this), this);
        getLogger().info("FareOrders enabled with " + orderManager.active().size() + " active orders.");
    }

    @Override public void onDisable() { if (database != null) database.close(); orderSessions.clear(); instance = null; }
    public static FareOrders getInstance() { return instance; }
    public OrderManager getOrderManager() { return orderManager; }
    public Database getDatabase() { return database; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public OrderService getOrderService() { return orderService; }
    public GUI getGui() { return gui; }
    public synchronized OrderSession getOrderSession(UUID uuid) { return orderSessions.get(uuid); }
    public synchronized OrderSession createOrderSession(UUID uuid) { OrderSession s = new OrderSession(uuid); orderSessions.put(uuid, s); return s; }
    public synchronized void removeOrderSession(UUID uuid) { orderSessions.remove(uuid); }
}
