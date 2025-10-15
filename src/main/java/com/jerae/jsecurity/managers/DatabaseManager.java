package com.jerae.jsecurity.managers;

import com.jerae.jsecurity.JSecurity;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final JSecurity plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(JSecurity plugin) {
        this.plugin = plugin;
        setupDataSource();
        initializeDatabase();
    }

    private void setupDataSource() {
        FileConfiguration config = plugin.getConfig();
        String databaseType = config.getString("database.type", "sqlite");

        HikariConfig hikariConfig = new HikariConfig();

        if (databaseType.equalsIgnoreCase("mysql")) {
            hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getString("database.host") + ":" + config.getInt("database.port") + "/" + config.getString("database.name"));
            hikariConfig.setUsername(config.getString("database.user"));
            hikariConfig.setPassword(config.getString("database.password"));
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + new File(plugin.getDataFolder(), "jsecurity.db").getAbsolutePath());
        }

        dataSource = new HikariDataSource(hikariConfig);
    }

    private void initializeDatabase() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            // Player Data Table
            statement.execute("CREATE TABLE IF NOT EXISTS player_data (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uuid VARCHAR(36) NOT NULL UNIQUE," +
                    "name VARCHAR(16) NOT NULL," +
                    "ips TEXT NOT NULL," +
                    "joined VARCHAR(255) NOT NULL," +
                    "password VARCHAR(255)," +
                    "notes TEXT" +
                    ");");

            // Punishments Table
            statement.execute("CREATE TABLE IF NOT EXISTS punishments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "type VARCHAR(16) NOT NULL," +
                    "reason VARCHAR(255)," +
                    "staff_name VARCHAR(16)," +
                    "end_time BIGINT" +
                    ");");

            // IP Bans Table
            statement.execute("CREATE TABLE IF NOT EXISTS ip_bans (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "ip_address VARCHAR(255) NOT NULL UNIQUE," +
                    "uuid VARCHAR(36) NOT NULL" +
                    ");");
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not initialize database: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}