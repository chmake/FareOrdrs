package com.fare.fareorders;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Locale;

public final class OrderListener implements Listener {
    private final FareOrders plugin;
    public OrderListener(FareOrders plugin){this.plugin=plugin;}

    @EventHandler public void onClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player p))return;
        String t=e.getView().getTitle();
        if(!isMenu(t))return;
        e.setCancelled(true);
        int slot=e.getRawSlot();
        if(slot<0||slot>=e.getView().getTopInventory().getSize())return;
        try{
            switch(t){case GUI.MAIN->main(p,slot);case GUI.CREATE->create(p,slot);case GUI.ITEMS->items(p,slot);case GUI.BROWSE->browse(p,slot);case GUI.MINE->mine(p,slot);case GUI.DETAIL->detail(p,slot);default->{}}
        }catch(Exception ex){plugin.getLogger().warning("GUI action failed: "+ex.getMessage());p.sendMessage(ChatColor.RED+"FareOrders » "+ex.getMessage());}
    }
    private void main(Player p,int s){
        if(s==20)plugin.getGui().openBrowse(p,0);
        else if(s==22){plugin.createOrderSession(p.getUniqueId());plugin.getGui().openCreateOrder(p);}
        else if(s==24)plugin.getGui().openMyOrders(p,0);
        else if(s==49)p.closeInventory();
    }
    private void create(Player p,int s){
        OrderSession x=session(p);
        if(s==20){x.setStage(OrderSession.Stage.SELECT_ITEM);plugin.getGui().openItemSelector(p,x.getItemPage(),x.getFilter());}
        else if(s==22){x.setInput(OrderSession.Input.AMOUNT);p.closeInventory();p.sendMessage(ChatColor.YELLOW+"FareOrders » Enter quantity in chat, or type cancel.");}
        else if(s==24){x.setInput(OrderSession.Input.PRICE);p.closeInventory();p.sendMessage(ChatColor.YELLOW+"FareOrders » Enter price per item in chat, or type cancel.");}
        else if(s==40&&x.getMaterial()!=null&&x.getAmount()>0&&x.getPrice()>0)confirm(p,x);
        else if(s==49){plugin.removeOrderSession(p.getUniqueId());p.closeInventory();}
    }
    private void items(Player p,int s){
        OrderSession x=session(p);
        if(s==45){plugin.getGui().openItemSelector(p,Math.max(0,x.getItemPage()-1),x.getFilter());return;}
        if(s==53){plugin.getGui().openItemSelector(p,x.getItemPage()+1,x.getFilter());return;}
        if(s==48){plugin.getGui().openCreateOrder(p);return;}
        if(s==49){x.setInput(OrderSession.Input.SEARCH);p.closeInventory();p.sendMessage(ChatColor.AQUA+"FareOrders » Enter an item name, or type cancel to clear the search.");return;}
        if(s>=0&&s<45){ItemStack item=p.getOpenInventory().getTopInventory().getItem(s);if(item!=null&&!item.getType().isAir()&&item.getType()!=org.bukkit.Material.GRAY_STAINED_GLASS_PANE){x.setMaterial(item.getType());x.setStage(OrderSession.Stage.ENTER_AMOUNT);x.setFilter("");x.setItemPage(0);plugin.getGui().openCreateOrder(p);}}
    }
    private void browse(Player p,int s){
        OrderSession x=session(p);List<Order> list=plugin.getOrderManager().active();int page=x.getBrowsePage();
        if(s==45){plugin.getGui().openBrowse(p,Math.max(0,page-1));return;}if(s==53){plugin.getGui().openBrowse(p,page+1);return;}if(s==49){plugin.getGui().openMainMenu(p);return;}
        if(s<45){int index=page*45+s;if(index<list.size()){Order o=list.get(index);plugin.getOrderService().expireIfNeeded(o);plugin.getGui().openOrderDetails(p,o);}}
    }
    private void mine(Player p,int s)throws Exception{
        OrderSession x=session(p);List<Order> list=plugin.getOrderManager().byOwner(p.getUniqueId());int page=x.getMinePage();
        if(s==45){plugin.getGui().openMyOrders(p,Math.max(0,page-1));return;}if(s==53){plugin.getGui().openMyOrders(p,page+1);return;}if(s==49){plugin.getGui().openMainMenu(p);return;}
        if(s<45){int index=page*45+s;if(index<list.size()){Order o=list.get(index);plugin.getOrderService().expireIfNeeded(o);plugin.getGui().openOrderDetails(p,o);}}
    }
    private void detail(Player p,int s)throws Exception{
        OrderSession x=session(p);if(s==49){plugin.getGui().openMainMenu(p);return;}long id=x.getSelectedOrderId();Order o=plugin.getOrderManager().get(id);
        if(o==null){p.sendMessage(ChatColor.RED+"FareOrders » Order not found.");plugin.getGui().openMainMenu(p);return;}
        plugin.getOrderService().expireIfNeeded(o);
        if(o.getOwner().equals(p.getUniqueId())){
            if(s==30){long got=plugin.getOrderService().claim(p,o);p.sendMessage(ChatColor.GREEN+"FareOrders » Collected "+got+" "+GUI.pretty(o.getMaterial())+".");plugin.getGui().openOrderDetails(p,o);}
            else if(s==32&&plugin.getOrderService().cancel(p,o)){p.sendMessage(ChatColor.YELLOW+"FareOrders » Order cancelled. Remaining funds refunded.");plugin.removeOrderSession(p.getUniqueId());plugin.getGui().openMyOrders(p,0);}
        }else if(!o.isComplete()&&!o.isExpired()){
            long want=s==20?1:s==22?64:s==24?o.getRemaining():0;if(want>0){long got=plugin.getOrderService().fulfill(p,o,want);p.sendMessage(ChatColor.GREEN+"FareOrders » Delivered "+got+" item(s) for "+plugin.getEconomyManager().format(got*o.getPrice())+".");plugin.getGui().openOrderDetails(p,o);}
        }
    }
    private void confirm(Player p,OrderSession x){try{if(plugin.getOrderManager().activeCount(p.getUniqueId())>=plugin.getConfig().getLong("orders.max-active-per-player",10))throw new IllegalStateException("You reached your active order limit.");Order o=plugin.getOrderService().create(p,x.getMaterial(),x.getAmount(),x.getPrice());plugin.removeOrderSession(p.getUniqueId());p.sendMessage(ChatColor.GREEN+"FareOrders » Order #"+o.getId()+" created. Reserved "+plugin.getEconomyManager().format(o.getTotalValue())+".");plugin.getGui().openMyOrders(p,0);}catch(Exception ex){p.sendMessage(ChatColor.RED+"FareOrders » "+ex.getMessage());}}
    @EventHandler public void onDrag(InventoryDragEvent e){if(isMenu(e.getView().getTitle()))e.setCancelled(true);}
    @EventHandler public void onChat(AsyncChatEvent e){Player p=e.getPlayer();OrderSession x=plugin.getOrderSession(p.getUniqueId());if(x==null||x.getInput()==OrderSession.Input.NONE)return;e.setCancelled(true);String text=PlainTextComponentSerializer.plainText().serialize(e.message()).trim();plugin.getServer().getScheduler().runTask(plugin,()->input(p,x,text));}
    private void input(Player p,OrderSession x,String text){
        if(text.equalsIgnoreCase("cancel")){x.setInput(OrderSession.Input.NONE);if(x.getInput()==OrderSession.Input.SEARCH)x.setFilter("");plugin.getGui().openCreateOrder(p);return;}
        try{switch(x.getInput()){
            case SEARCH->{x.setFilter(text.toLowerCase(Locale.ROOT));x.setItemPage(0);x.setInput(OrderSession.Input.NONE);plugin.getGui().openItemSelector(p,0,x.getFilter());}
            case AMOUNT->{long n=Long.parseLong(text);if(n<=0)throw new IllegalArgumentException("Quantity must be positive.");x.setAmount(n);x.setInput(OrderSession.Input.NONE);x.setStage(OrderSession.Stage.ENTER_PRICE);plugin.getGui().openCreateOrder(p);}
            case PRICE->{double n=Double.parseDouble(text);if(!Double.isFinite(n)||n<=0)throw new IllegalArgumentException("Price must be positive.");x.setPrice(n);x.setInput(OrderSession.Input.NONE);x.setStage(OrderSession.Stage.CONFIRM);plugin.getGui().openCreateOrder(p);}
            default->{}}
        }catch(NumberFormatException ex){p.sendMessage(ChatColor.RED+"FareOrders » Invalid number. Try again or type cancel.");}catch(Exception ex){p.sendMessage(ChatColor.RED+"FareOrders » "+ex.getMessage());}
    }
    private OrderSession session(Player p){OrderSession x=plugin.getOrderSession(p.getUniqueId());return x==null?plugin.createOrderSession(p.getUniqueId()):x;}
    private boolean isMenu(String t){return GUI.MAIN.equals(t)||GUI.CREATE.equals(t)||GUI.ITEMS.equals(t)||GUI.BROWSE.equals(t)||GUI.MINE.equals(t)||GUI.DETAIL.equals(t);}
}
