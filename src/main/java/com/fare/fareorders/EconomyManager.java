package com.fare.fareorders;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyManager {
    private final FareOrders plugin;
    private Economy economy;
    public EconomyManager(FareOrders plugin) { this.plugin = plugin; }
    public boolean setup() {
        RegisteredServiceProvider<Economy> provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) { economy = null; plugin.getLogger().warning("No Vault economy provider detected."); return false; }
        economy = provider.getProvider();
        plugin.getLogger().info("Economy detected: " + economy.getName());
        return true;
    }
    public boolean isAvailable() { if (economy == null) setup(); return economy != null; }
    public Economy getEconomy() { return economy; }
    public boolean has(OfflinePlayer player, double amount) { return isAvailable() && Double.isFinite(amount) && amount >= 0 && economy.has(player, amount); }
    public boolean withdraw(OfflinePlayer player, double amount) { return isAvailable() && Double.isFinite(amount) && amount >= 0 && economy.withdrawPlayer(player, amount).transactionSuccess(); }
    public boolean deposit(OfflinePlayer player, double amount) { return isAvailable() && Double.isFinite(amount) && amount >= 0 && economy.depositPlayer(player, amount).transactionSuccess(); }
    public String format(double amount) { return isAvailable() ? economy.format(amount) : String.format("%.2f", amount); }
}
