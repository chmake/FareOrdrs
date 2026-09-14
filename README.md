# FareOrders

FareOrders is a lightweight player-to-player buy-order marketplace for Paper 1.21.x.

## Features

- `/orders` is the only player command.
- GUI-driven browsing, creation and management.
- Searchable vanilla Material selector.
- Quantity and price input through chat; no additional commands.
- Funds are reserved when an order is created.
- Other players can deliver 1, 64, or all matching items they own.
- Sellers are paid through Vault immediately after a successful delivery.
- Buyers collect delivered items from their order GUI.
- Cancellation refunds the unfulfilled portion.
- Expiration refunds the unfulfilled portion while preserving delivered items.
- SQLite persistence.
- Vault economy auto-detection.
- Configurable order limits and expiration.
- No MySQL, Redis, NMS, or required server-specific economy plugin.

## Dependencies

- Paper 1.21.x
- Vault
- A Vault-compatible economy provider

The SQLite JDBC driver is bundled in the plugin jar.

## Build

```bash
mvn -DskipTests package
```

The output is `target/FareOrders.jar`.

A manual GitHub Actions build is also provided under `.github/workflows/build.yml`.

## Install

1. Put `FareOrders.jar` in `plugins/`.
2. Install Vault and a Vault-compatible economy plugin.
3. Start the server.
4. Use `/orders`.

## Data

FareOrders stores its SQLite database at `plugins/FareOrders/orders.db`.

The current item matcher is Material-based. That means an order for DIAMOND accepts normal diamond items, while custom NBT/metadata matching is intentionally not claimed as supported yet.
