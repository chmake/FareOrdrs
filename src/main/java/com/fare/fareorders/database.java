package com.fare.fareorders;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Database {

    private final FareOrders plugin;
    private Connection connection;

    public Database(FareOrders plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        File databaseFile = new File(plugin.getDataFolder(), "orders.db");

        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new SQLException("Could not create plugin data folder.");
        }

        connection = DriverManager.getConnection(
                "jdbc:sqlite:" + databaseFile.getAbsolutePath()
        );

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id INTEGER PRIMARY KEY,
                        owner TEXT NOT NULL,
                        item TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        price REAL NOT NULL,
                        created_at INTEGER NOT NULL,
                        expires_at INTEGER NOT NULL,
                        remaining INTEGER NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE INDEX IF NOT EXISTS idx_orders_owner
                    ON orders(owner)
                    """);

            statement.executeUpdate("""
                    CREATE INDEX IF NOT EXISTS idx_orders_remaining
                    ON orders(remaining)
                    """);
        }
    }

    public void close() {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException exception) {
            plugin.getLogger().warning(
                    "Failed to close database: " + exception.getMessage()
            );
        }

        connection = null;
    }

    public void saveOrder(Order order) throws SQLException {
        String sql = """
                INSERT OR REPLACE INTO orders
                (id, owner, item, amount, price, created_at, expires_at, remaining)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, order.getId());
            statement.setString(2, order.getOwner().toString());
            statement.setString(3, order.getItem());
            statement.setLong(4, order.getAmount());
            statement.setDouble(5, order.getPrice());
            statement.setLong(6, order.getCreatedAt());
            statement.setLong(7, order.getExpiresAt());
            statement.setLong(8, order.getRemaining());

            statement.executeUpdate();
        }
    }

    public void deleteOrder(long id) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM orders WHERE id = ?")) {

            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public Order getOrder(long id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return readOrder(result);
            }
        }
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT * FROM orders ORDER BY id DESC");
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                orders.add(readOrder(result));
            }
        }

        return orders;
    }

    private Order readOrder(ResultSet result) throws SQLException {
        return new Order(
                result.getLong("id"),
                UUID.fromString(result.getString("owner")),
                result.getString("item"),
                result.getLong("amount"),
                result.getDouble("price"),
                result.getLong("created_at"),
                result.getLong("expires_at"),
                result.getLong("remaining")
        );
    }
}
