package com.fare.fareorders;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.text.PaperComponents;
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
    public OrderListener(FareOrders plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        String title = event.getView().getTitle();
        if (!isFareMenu(title)) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) return;

        try {
            switch (title) {
                case GUI.MAIN -> main(p, slot);
                case GUI.CREATE -> create(p, slot);
                case GUI.ITEMS -> items(p, slot);
                case GUI.BROWSE -> browse(p, slot);
                case GUI.MINE -> mine(p, slot);
                case GUI.DETAIL -> detail(p, slot);
                default -> { }
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("GUI action failed: " + ex.getMessage());
            p.sendMessage(ChatColor.RED + "FareOrders » Something went wrong. Check the server console.");
        }
    }

    private void main(Player p, int slot) {
        if (slot == 20) plugin.getGui().openBrowse(p, 0);
        else if (slot == 22) { plugin.createOrderSession(p.getUniqueId()); plugin.getGui().openCreateOrder(p); }
        else if (slot == 24) plugin.getGui().openMyOrders(p, 0);
        else if (slot == 49) p.closeInventory();
    }

    private void create(Player p, int slot) {
        OrderSession s = session(p);
        if (slot == 20) { s.setStage(OrderSession.Stage.SELECT_ITEM); plugin.getGui().openItemSelector(p, s.getItemPage(), s.getFilter()); }
        else if (slot == 22) { s.setInput(OrderSession.Input.AMOUNT); p.closeInventory(); p.sendMessage(ChatColor.YELLOW + "FareOrders » Enter the quantity in chat. Type " + ChatColor.WHITE + "cancel" + ChatColor.YELLOW + " to stop."); }
        else if (slot == 24) { s.setInput(OrderSession.Input.PRICE); p.closeInventory(); p.sendMessage(ChatColor.YELLOW + "FareOrders » Enter the price per item in chat. Type " + ChatColor.WHITE + "cancel" + ChatColor.YELLOW + " to stop."); }
        else if (slot == 40 && s.getMaterial() != null && s.getAmount() > 0 && s.getPrice() > 0) confirmCreate(p, s);
        else if (slot == 49) { plugin.removeOrderSession(p.getUniqueId()); p.closeInventory(); }
    }

    private void items(Player p, int slot) {
        OrderSession s = session(p);
        if (slot == 45) { if (s.getItemPage() > 0) s.setItemPage(s.getItemPage() - 1); plugin.getGui().openItemSelector(p, s.getItemPage(), s.getFilter()); return; }
        if (slot == 53) { s.setItemPage(s.getItemPage() + 1); plugin.getGui().openItemSelector(p, s.getItemPage(), s.getFilter()); return; }
        if (slot == 49) { s.setInput(OrderSession.Input.SEARCH); p.closeInventory(); p.sendMessage(ChatColor.AQUA + "FareOrders » Search by item name. Type " + ChatColor.WHITE + "cancel" + ChatColor.AQUA + " to clear/stop."); return; }
        if (slot == 48) { plugin.getGui().openCreateOrder(p); return; }
        if (slot >= 0 && slot < 45) {
            ItemStack clicked = eventItem(p, slot);
            if (clicked != null && clicked.getType() != org.bukkit.Material.GRAY_STAINED_GLASS_PANE) {
                s.setMaterial(clicked.getType()); s.setStage(OrderSession.Stage.ENTER_AMOUNT); s.setInput(OrderSession.Input.NONE); plugin.getGui().openCreateOrder(p);
            }
        }
    }

    private ItemStack eventItem(Player p, int slot) { return p.getOpenInventory().getTopInventory().getItem(slot); }

    private void browse(Player p, int slot) {
        List<Order> list = plugin.getOrderManager().active();
        if (slot == 45) { int page = pageFromSlot(list, p); plugin.getGui().openBrowse(p, Math.max(0, page - 1)); return; }
        if (slot == 53) { int page = pageFromSlot(list, p); plugin.getGui().openBrowse(p, page + 1); return; }
        if (slot == 49) { plugin.getGui().openMainMenu(p); return; }
        if (slot >= 0 && slot < 45) {
            int page = pageFromSlot(list, p); int index = page * 45 + slot;
            if (index < list.size()) { Order o = list.get(index); plugin.getOrderService().expireIfNeeded(o); plugin.getGui().openOrderDetails(p, o); }
        }
    }

    private void mine(Player p, int slot) {
        List<Order> list = plugin.getOrderManager().byOwner(p.getUniqueId());
        if (slot == 45) { int page = pageFromSlot(list, p); plugin.getGui().openMyOrders(p, Math.max(0, page - 1)); return; }
        if (slot == 53) { int page = pageFromSlot(list, p); plugin.getGui().openMyOrders(p, page + 1); return; }
        if (slot == 49) { plugin.getGui().openMainMenu(p); return; }
        if (slot >= 0 && slot < 45) {
            int page = pageFromSlot(list, p); int index = page * 45 + slot;
            if (index < list.size()) { Order o = list.get(index); plugin.getOrderService().expireIfNeeded(o); plugin.getGui().openOrderDetails(p, o); }
        }
    }

    private void detail(Player p, int slot) throws Exception {
        // The selected order id is encoded by the session while opening details.
        OrderSession s = plugin.getOrderSession(p.getUniqueId());
        if (slot == 49) { plugin.getGui().openMainMenu(p); return; }
        if (s == null || s.getSelectedOrderId() <= 0) { plugin.getGui().openMainMenu(p); return; }
        Order o = plugin.getOrderManager().get(s.getSelectedOrderId());
        if (o == null) { p.sendMessage(ChatColor.RED + "FareOrders » Order no longer exists."); plugin.getGui().openMainMenu(p); return; }
        plugin.getOrderService().expireIfNeeded(o);
        if (o.getOwner().equals(p.getUniqueId())) {
            if (slot == 30) { long got = plugin.getOrderService().claim(p, o); p.sendMessage(ChatColor.GREEN + "FareOrders » Collected " + got + " " + GUI.pretty(o.getMaterial()) + "."); plugin.getGui().openOrderDetails(p, o); }
            else if (slot == 32) { if (plugin.getOrderService().cancel(p, o)) { p.sendMessage(ChatColor.YELLOW + "FareOrders » Order cancelled and remaining funds refunded."); plugin.getGui().openMyOrders(p, 0); } }
        } else if (!o.isComplete() && !o.isExpired()) {
            long requested = slot == 20 ? 1 : slot == 22 ? 64 : slot == 24 ? o.getRemaining() : 0;
            if (requested > 0) { long delivered = plugin.getOrderService().fulfill(p, o, requested); p.sendMessage(ChatColor.GREEN + "FareOrders » Delivered " + delivered + " item(s) for " + plugin.getEconomyManager().format(delivered * o.getPrice()) + "."); plugin.getGui().openOrderDetails(p, o); }
        }
    }

    private void confirmCreate(Player p, OrderSession s) {
        try {
            if (plugin.getOrderManager().activeCount(p.getUniqueId()) >= plugin.getConfig().getLong("orders.max-active-per-player", 10)) throw new IllegalStateException("You have reached your active order limit.");
            Order o = plugin.getOrderService().create(p, s.getMaterial(), s.getAmount(), s.getPrice());
            plugin.removeOrderSession(p.getUniqueId());
            p.sendMessage(ChatColor.GREEN + "FareOrders » Order #" + o.getId() + " created. Reserved: " + plugin.getEconomyManager().format(o.getTotalValue()) + ".");
            plugin.getGui().openMyOrders(p, 0);
        } catch (Exception ex) { p.sendMessage(ChatColor.RED + "FareOrders » " + ex.getMessage()); }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) { if (isFareMenu(event.getView().getTitle())) event.setCancelled(true); }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player p = event.getPlayer();
        OrderSession s = plugin.getOrderSession(p.getUniqueId());
        if (s == null || s.getInput() == OrderSession.Input.NONE) return;
        event.setCancelled(true);
        String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        plugin.getServer().getScheduler().runTask(plugin, () -> handleInput(p, s, text));
    }

    private void handleInput(Player p, OrderSession s, String text) {
        if (text.equalsIgnoreCase("cancel")) { s.setInput(OrderSession.Input.NONE); s.setFilter(""); plugin.getGui().openCreateOrder(p); return; }
        try {
            switch (s.getInput()) {
                case SEARCH -> { s.setFilter(text.toLowerCase(Locale.ROOT)); s.setItemPage(0); s.setInput(OrderSession.Input.NONE); plugin.getGui().openItemSelector(p, 0, s.getFilter()); }
                case AMOUNT -> { long amount = Long.parseLong(text); if (amount <= 0) throw new IllegalArgumentException("Quantity must be positive."); s.setAmount(amount); s.setInput(OrderSession.Input.NONE); s.setStage(OrderSession.Stage.ENTER_PRICE); plugin.getGui().openCreateOrder(p); }
                case PRICE -> { double price = Double.parseDouble(text); if (!Double.isFinite(price) || price <= 0) throw new IllegalArgumentException("Price must be a positive number."); s.setPrice(price); s.setInput(OrderSession.Input.NONE); s.setStage(OrderSession.Stage.CONFIRM); plugin.getGui().openCreateOrder(p); }
                default -> { }
            }
        } catch (NumberFormatException ex) { p.sendMessage(ChatColor.RED + "FareOrders » Invalid number. Try again or type cancel."); }
          catch (Exception ex) { p.sendMessage(ChatColor.RED + "FareOrders » " + ex.getMessage()); }
    }

    private OrderSession session(Player p) { OrderSession s = plugin.getOrderSession(p.getUniqueId()); return s == null ? plugin.createOrderSession(p.getUniqueId()) : s; }
    private boolean isFareMenu(String title) { return GUI.MAIN.equals(title) || GUI.CREATE.equals(title) || GUI.ITEMS.equals(title) || GUI.BROWSE.equals(title) || GUI.MINE.equals(title) || GUI.DETAIL.equals(title); }
    private int pageFromSlot(List<Order> list, Player p) { int size = p.getOpenInventory().getTopInventory().getSize(); if (size != 54) return 0; for (int page = 0; page <= Math.max(0, (list.size()-1)/45); page++) { return page; } return 0; }
}
