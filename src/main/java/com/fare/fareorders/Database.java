package com.fare.fareorders;

import org.bukkit.Material;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Database {
    private final FareOrders plugin;
    private Connection connection;

    public Database(FareOrders plugin) { this.plugin = plugin; }

    public synchronized void connect() throws SQLException {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs())
            throw new SQLException("Could not create plugin data folder.");
        connection = DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "orders.db").getAbsolutePath());
        try (Statement s = connection.createStatement()) {
            s.execute("PRAGMA journal_mode=WAL");
            s.execute("PRAGMA foreign_keys=ON");
            s.execute("CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY, owner TEXT NOT NULL, material TEXT NOT NULL, amount INTEGER NOT NULL, price REAL NOT NULL, created_at INTEGER NOT NULL, expires_at INTEGER NOT NULL, remaining INTEGER NOT NULL, delivered INTEGER NOT NULL DEFAULT 0)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_orders_owner ON orders(owner)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_orders_active ON orders(remaining, expires_at)");
        }
        migrateLegacyTable();
    }

    private void migrateLegacyTable() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("PRAGMA table_info(orders)"); ResultSet rs = ps.executeQuery()) {
            boolean material = false, delivered = false;
            while (rs.next()) {
                String name = rs.getString("name");
                material |= "material".equalsIgnoreCase(name);
                delivered |= "delivered".equalsIgnoreCase(name);
            }
            if (!material) throw new SQLException("Existing orders database uses the old item schema. Delete orders.db before first production use.");
            if (!delivered) try (Statement s = connection.createStatement()) { s.execute("ALTER TABLE orders ADD COLUMN delivered INTEGER NOT NULL DEFAULT 0"); }
        }
    }

    public synchronized void close() {
        if (connection != null) try { connection.close(); } catch (SQLException e) { plugin.getLogger().warning("SQLite close failed: " + e.getMessage()); }
        connection = null;
    }

    public synchronized List<Order> getAllOrders() throws SQLException {
        List<Order> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM orders ORDER BY id DESC"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(read(rs));
        }
        return result;
    }

    public synchronized Order getOrder(long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM orders WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? read(rs) : null; }
        }
    }

    public synchronized void insertOrder(Order o) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO orders(id,owner,material,amount,price,created_at,expires_at,remaining,delivered) VALUES(?,?,?,?,?,?,?,?,?)")) {
            ps.setLong(1,o.getId()); ps.setString(2,o.getOwner().toString()); ps.setString(3,o.getMaterial().name()); ps.setLong(4,o.getAmount()); ps.setDouble(5,o.getPrice()); ps.setLong(6,o.getCreatedAt()); ps.setLong(7,o.getExpiresAt()); ps.setLong(8,o.getRemaining()); ps.setLong(9,o.getDelivered()); ps.executeUpdate();
        }
    }

    public synchronized void updateOrder(Order o) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE orders SET remaining=?, delivered=? WHERE id=?")) {
            ps.setLong(1,o.getRemaining()); ps.setLong(2,o.getDelivered()); ps.setLong(3,o.getId()); ps.executeUpdate();
        }
    }

    public synchronized void deleteOrder(long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM orders WHERE id=?")) { ps.setLong(1,id); ps.executeUpdate(); }
    }

    public synchronized long maxId() throws SQLException {
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery("SELECT COALESCE(MAX(id),0) FROM orders")) { return rs.next() ? rs.getLong(1) : 0; }
    }

    private Order read(ResultSet rs) throws SQLException {
        Material m = Material.matchMaterial(rs.getString("material"));
        if (m == null) throw new SQLException("Unknown material in order " + rs.getLong("id"));
        return new Order(rs.getLong("id"), UUID.fromString(rs.getString("owner")), m, rs.getLong("amount"), rs.getDouble("price"), rs.getLong("created_at"), rs.getLong("expires_at"), rs.getLong("remaining"), rs.getLong("delivered"));
    }
}
