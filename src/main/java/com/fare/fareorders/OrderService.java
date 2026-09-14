package com.fare.fareorders;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.sql.SQLException;

public final class OrderService {
    private final FareOrders plugin;
    public OrderService(FareOrders plugin){this.plugin=plugin;}

    public Order create(Player buyer,Material material,long amount,double price)throws Exception{
        validate(material,amount,price);double total=amount*price;
        if(!Double.isFinite(total)||total<=0)throw new IllegalArgumentException("Order total is invalid.");
        if(!plugin.getEconomyManager().isAvailable())throw new IllegalStateException("No economy provider is available.");
        if(!plugin.getEconomyManager().has(buyer,total))throw new IllegalStateException("You cannot afford this order.");
        if(!plugin.getEconomyManager().withdraw(buyer,total))throw new IllegalStateException("Could not reserve the order funds.");
        try{return plugin.getOrderManager().create(buyer.getUniqueId(),material,amount,price,plugin.getConfig().getLong("orders.expiration-seconds",86400L)*1000L);}
        catch(Exception ex){plugin.getEconomyManager().deposit(buyer,total);throw ex;}
    }

    public long fulfill(Player fulfiller,Order order,long requested)throws Exception{
        if(order==null)throw new IllegalArgumentException("Order not found.");
        if(order.getOwner().equals(fulfiller.getUniqueId()))throw new IllegalArgumentException("You cannot fulfill your own order.");
        expireIfNeeded(order);if(order.isExpired())throw new IllegalStateException("This order has expired.");
        long amount=Math.min(requested,order.getRemaining());if(amount<=0)throw new IllegalStateException("Nothing remains on this order.");
        amount=Math.min(amount,count(fulfiller,order.getMaterial()));if(amount<=0)throw new IllegalStateException("You do not have the requested item.");
        remove(fulfiller,order.getMaterial(),amount);
        boolean changed=false;
        try{
            plugin.getOrderManager().fulfill(order,amount);changed=true;
            double payout=amount*order.getPrice();
            if(!plugin.getEconomyManager().deposit(fulfiller,payout))throw new IllegalStateException("Payout failed.");
            return amount;
        }catch(Exception ex){
            if(changed){order.setRemaining(order.getRemaining()+amount);order.setDelivered(order.getDelivered()-amount);try{plugin.getOrderManager().save(order);}catch(Exception saveEx){plugin.getLogger().severe("CRITICAL: failed to roll back order #"+order.getId()+": "+saveEx.getMessage());}}
            give(fulfiller,order.getMaterial(),amount);
            throw new IllegalStateException("Delivery failed; your items were restored.",ex);
        }
    }

    public long claim(Player buyer,Order order)throws SQLException{
        if(order==null||!order.getOwner().equals(buyer.getUniqueId()))return 0;long claimable=order.getDelivered();if(claimable<=0)return 0;
        long capacity=countFreeCapacity(buyer,order.getMaterial());long amount=Math.min(claimable,capacity);if(amount<=0)return 0;
        give(buyer,order.getMaterial(),amount);order.setDelivered(order.getDelivered()-amount);plugin.getOrderManager().save(order);return amount;
    }

    public boolean cancel(Player buyer,Order order)throws SQLException{
        if(order==null||!order.getOwner().equals(buyer.getUniqueId())||order.isComplete())return false;
        double refund=order.getRemainingValue();plugin.getOrderManager().delete(order);if(refund>0&&!plugin.getEconomyManager().deposit(buyer,refund))throw new SQLException("Order deleted but refund failed; contact an administrator.");return true;
    }

    public boolean expireIfNeeded(Order order)throws SQLException{
        if(order==null||!order.isExpired()||order.getRemaining()<=0)return false;double refund=order.getRemainingValue();order.setRemaining(0);plugin.getOrderManager().save(order);
        if(refund>0&&!plugin.getEconomyManager().deposit(plugin.getServer().getOfflinePlayer(order.getOwner()),refund))plugin.getLogger().severe("CRITICAL: expiration refund failed for order #"+order.getId()+" amount "+refund);return true;
    }

    private void validate(Material material,long amount,double price){
        if(material==null||material.isAir())throw new IllegalArgumentException("Invalid item.");
        if(amount<plugin.getConfig().getLong("orders.min-quantity",1))throw new IllegalArgumentException("Quantity is below the minimum.");
        if(amount>plugin.getConfig().getLong("orders.max-quantity",2304))throw new IllegalArgumentException("Quantity is above the maximum.");
        if(!Double.isFinite(price)||price<plugin.getConfig().getDouble("orders.min-price",0.01)||price>plugin.getConfig().getDouble("orders.max-price",1.0E12))throw new IllegalArgumentException("Price is outside the allowed range.");
    }
    private long count(Player p,Material m){long n=0;for(ItemStack s:p.getInventory().getStorageContents())if(s!=null&&s.getType()==m)n+=s.getAmount();return n;}
    private void remove(Player p,Material m,long amount){ItemStack[] c=p.getInventory().getStorageContents();long left=amount;for(int i=0;i<c.length&&left>0;i++){ItemStack s=c[i];if(s==null||s.getType()!=m)continue;int take=(int)Math.min(left,s.getAmount());s.setAmount(s.getAmount()-take);left-=take;if(s.getAmount()<=0)c[i]=null;}p.getInventory().setStorageContents(c);}
    private long countFreeCapacity(Player p,Material m){long slots=0;int max=m.getMaxStackSize();for(ItemStack s:p.getInventory().getStorageContents()){if(s==null||s.getType().isAir())slots+=max;else if(s.getType()==m)slots+=Math.max(0,max-s.getAmount());}return slots;}
    private void give(Player p,Material m,long amount){long left=amount;while(left>0){int give=(int)Math.min(left,m.getMaxStackSize());for(ItemStack rest:p.getInventory().addItem(new ItemStack(m,give)).values())p.getWorld().dropItemNaturally(p.getLocation(),rest);left-=give;}}
}
