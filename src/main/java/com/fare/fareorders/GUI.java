package com.fare.fareorders;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class GUI {
    public static final String MAIN = ChatColor.DARK_GRAY + "FareOrders";
    public static final String CREATE = ChatColor.DARK_GREEN + "Create Order";
    public static final String ITEMS = ChatColor.DARK_AQUA + "Select Item";
    public static final String BROWSE = ChatColor.DARK_BLUE + "Browse Orders";
    public static final String MINE = ChatColor.DARK_PURPLE + "My Orders";
    public static final String DETAIL = ChatColor.DARK_GRAY + "Order Details";

    private final FareOrders plugin;
    public GUI(FareOrders plugin) { this.plugin = plugin; }

    public void openMainMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, MAIN);
        fill(inv);
        inv.setItem(20, item(Material.CHEST, ChatColor.GOLD + "Browse Orders", "View orders from other players."));
        inv.setItem(22, item(Material.EMERALD, ChatColor.GREEN + "Create Order", "Request items from other players."));
        inv.setItem(24, item(Material.BOOK, ChatColor.YELLOW + "My Orders", "Manage your orders and deliveries."));
        inv.setItem(49, item(Material.BARRIER, ChatColor.RED + "Close"));
        p.openInventory(inv);
    }

    public void openCreateOrder(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, CREATE);
        fill(inv);
        OrderSession s = plugin.getOrderSession(p.getUniqueId());
        String selected = s != null && s.getMaterial() != null ? pretty(s.getMaterial()) : "Not selected";
        inv.setItem(20, item(s != null && s.getMaterial() != null ? s.getMaterial() : Material.DIAMOND, ChatColor.AQUA + "Item", "Selected: " + selected, "Click to choose an item."));
        inv.setItem(22, item(Material.PAPER, ChatColor.YELLOW + "Quantity", s != null && s.getAmount() > 0 ? "Amount: " + s.getAmount() : "Click to enter quantity."));
        inv.setItem(24, item(Material.EMERALD, ChatColor.GREEN + "Price", s != null && s.getPrice() > 0 ? "Per item: " + plugin.getEconomyManager().format(s.getPrice()) : "Click to enter price."));
        boolean ready = s != null && s.getMaterial() != null && s.getAmount() > 0 && s.getPrice() > 0;
        inv.setItem(40, item(ready ? Material.LIME_DYE : Material.GRAY_DYE, ready ? ChatColor.GREEN + "Confirm Order" : ChatColor.GRAY + "Confirm Order", ready ? "Reserve funds and create the order." : "Choose an item, quantity and price first."));
        inv.setItem(49, item(Material.BARRIER, ChatColor.RED + "Cancel"));
        p.openInventory(inv);
    }

    public void openItemSelector(Player p, int page, String filter) {
        Inventory inv = Bukkit.createInventory(null, 54, ITEMS);
        fill(inv);
        List<Material> mats = new ArrayList<>();
        String f = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
        for (Material m : Material.values()) {
            if (!m.isItem() || m.isAir() || !m.isSolid() && !m.isEdible() && m.getMaxStackSize() <= 0) continue;
            if (!f.isEmpty() && !m.name().toLowerCase(Locale.ROOT).contains(f)) continue;
            mats.add(m);
        }
        mats.sort(Comparator.comparing(Material::name));
        int maxPage = Math.max(0, (mats.size() - 1) / 45);
        page = Math.max(0, Math.min(page, maxPage));
        int start = page * 45;
        for (int i = 0; i < 45 && start + i < mats.size(); i++) {
            Material m = mats.get(start + i);
            inv.setItem(i, item(m, ChatColor.WHITE + pretty(m), "Click to select this item."));
        }
        inv.setItem(45, item(Material.ARROW, ChatColor.YELLOW + "Previous", page > 0 ? "Go back a page." : "First page."));
        inv.setItem(49, item(Material.NAME_TAG, ChatColor.AQUA + "Search", filter == null || filter.isBlank() ? "Enter a name in chat." : "Current: " + filter));
        inv.setItem(53, item(Material.ARROW, ChatColor.YELLOW + "Next", page < maxPage ? "Go forward a page." : "Last page."));
        inv.setItem(48, item(Material.BARRIER, ChatColor.RED + "Back"));
        inv.setItem(50, item(Material.BOOK, ChatColor.GRAY + "Page " + (page + 1) + "/" + (maxPage + 1)));
        p.openInventory(inv);
    }

    public void openBrowse(Player p, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, BROWSE);
        fill(inv);
        List<Order> list = plugin.getOrderManager().active();
        int maxPage = Math.max(0, (list.size() - 1) / 45);
        page = Math.max(0, Math.min(page, maxPage));
        int start = page * 45;
        for (int i = 0; i < 45 && start + i < list.size(); i++) {
            Order o = list.get(start + i);
            inv.setItem(i, orderIcon(o));
        }
        nav(inv, page, maxPage, Material.ARROW, 45, 53, 49);
        p.openInventory(inv);
    }

    public void openMyOrders(Player p, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, MINE);
        fill(inv);
        List<Order> list = plugin.getOrderManager().byOwner(p.getUniqueId());
        int maxPage = Math.max(0, (list.size() - 1) / 45);
        page = Math.max(0, Math.min(page, maxPage));
        int start = page * 45;
        for (int i = 0; i < 45 && start + i < list.size(); i++) inv.setItem(i, orderIcon(list.get(start + i)));
        nav(inv, page, maxPage, Material.ARROW, 45, 53, 49);
        p.openInventory(inv);
    }

    public void openOrderDetails(Player p, Order o) {
        Inventory inv = Bukkit.createInventory(null, 54, DETAIL);
        fill(inv);
        if (o == null) { inv.setItem(22, item(Material.BARRIER, ChatColor.RED + "Order not found")); p.openInventory(inv); return; }
        boolean mine = o.getOwner().equals(p.getUniqueId());
        inv.setItem(13, item(o.getMaterial(), ChatColor.AQUA + pretty(o.getMaterial()), "Order #" + o.getId(), "Requested: " + o.getAmount(), "Remaining: " + o.getRemaining(), "Delivered: " + o.getDelivered(), "Price: " + plugin.getEconomyManager().format(o.getPrice()) + " each", "Total: " + plugin.getEconomyManager().format(o.getTotalValue())));
        if (mine) {
            inv.setItem(30, item(Material.HOPPER, ChatColor.GREEN + "Collect Delivered", "Items waiting: " + o.getDelivered(), "Click to collect as much as fits."));
            inv.setItem(32, item(Material.BARRIER, ChatColor.RED + "Cancel Order", "Refunds all unfulfilled funds.", "Delivered items remain claimable."));
        } else if (!o.isComplete() && !o.isExpired()) {
            inv.setItem(20, item(Material.IRON_INGOT, ChatColor.YELLOW + "Deliver 1", "Supply one item."));
            inv.setItem(22, item(Material.GOLD_INGOT, ChatColor.GOLD + "Deliver 64", "Supply up to 64 items."));
            inv.setItem(24, item(Material.DIAMOND, ChatColor.AQUA + "Deliver All", "Supply as many as you have."));
        }
        inv.setItem(49, item(Material.ARROW, ChatColor.YELLOW + "Back"));
        p.openInventory(inv);
    }

    private void nav(Inventory inv, int page, int max, Material mat, int prev, int next, int back) {
        inv.setItem(prev, item(mat, ChatColor.YELLOW + "Previous", page > 0 ? "Previous page." : "First page."));
        inv.setItem(49, item(Material.BARRIER, ChatColor.RED + "Back"));
        inv.setItem(next, item(mat, ChatColor.YELLOW + "Next", page < max ? "Next page." : "Last page."));
        inv.setItem(50, item(Material.BOOK, ChatColor.GRAY + "Page " + (page + 1) + "/" + (max + 1)));
    }

    private ItemStack orderIcon(Order o) {
        return item(o.getMaterial(), ChatColor.AQUA + pretty(o.getMaterial()), "Order #" + o.getId(), "Remaining: " + o.getRemaining() + "/" + o.getAmount(), "Price: " + plugin.getEconomyManager().format(o.getPrice()) + " each", "Delivered: " + o.getDelivered(), "Click for details.");
    }

    private void fill(Inventory inv) {
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);
    }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material == null ? Material.BARRIER : material);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;
        meta.setDisplayName(name);
        if (lore.length > 0) meta.setLore(List.of(lore));
        stack.setItemMeta(meta);
        return stack;
    }

    public static String pretty(Material m) {
        String[] parts = m.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder b = new StringBuilder();
        for (String part : parts) { if (b.length() > 0) b.append(' '); b.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)); }
        return b.toString();
    }
}
