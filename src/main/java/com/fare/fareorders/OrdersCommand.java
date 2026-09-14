package com.fare.fareorders;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class OrdersCommand implements CommandExecutor {
    private final FareOrders plugin;
    public OrdersCommand(FareOrders plugin){this.plugin=plugin;}
    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        if(!(sender instanceof Player player)){sender.sendMessage("Only players can use /orders.");return true;}
        plugin.getGui().openMainMenu(player);
        return true;
    }
}
