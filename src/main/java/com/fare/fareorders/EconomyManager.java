package com.fare.fareorders;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyManager {

    private final FareOrders plugin;
    private Economy economy;

    public EconomyManager(FareOrders plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault was not found.");
            return false;
        }

        RegisteredServiceProvider<Economy> provider =
                plugin.getServer()
                        .getServicesManager()
                        .getRegistration(Economy.class);

        if (provider == null) {
            plugin.getLogger().warning("No economy provider was found.");
            return false;
        }

        economy = provider.getProvider();

        plugin.getLogger().info(
                "Economy detected: " + economy.getName()
        );

        return true;
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean has(org.bukkit.OfflinePlayer player, double amount) {
        return economy != null && economy.has(player, amount);
    }

    public boolean withdraw(org.bukkit.OfflinePlayer player, double amount) {
        if (economy == null) {
            return false;
        }

        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(org.bukkit.OfflinePlayer player, double amount) {
        if (economy == null) {
            return false;
        }

        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public String format(double amount) {
        if (economy == null) {
            return String.format("%.2f", amount);
        }

        return economy.format(amount);
    }
}
