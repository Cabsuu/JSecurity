package com.jerae.jsecurity.managers;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.models.PlayerData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlayerDataManager {
    private final JSecurity plugin;
    private final DatabaseManager databaseManager;

    public PlayerDataManager(JSecurity plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public PlayerData getPlayerData(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_data WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new PlayerData(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        new ArrayList<>(Arrays.asList(rs.getString("ips").split(","))),
                        rs.getString("joined")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve player data: " + e.getMessage());
        }
        return null;
    }

    public PlayerData getPlayerData(String name) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_data WHERE name = ?")) {
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new PlayerData(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        new ArrayList<>(Arrays.asList(rs.getString("ips").split(","))),
                        rs.getString("joined")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve player data: " + e.getMessage());
        }
        return null;
    }

    public void createPlayerData(UUID uuid, String name, String ip) {
        if (getPlayerData(uuid) == null) {
            String joined = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm").format(java.time.LocalDateTime.now());
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO player_data (uuid, name, ips, joined) VALUES (?, ?, ?, ?)")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, name);
                statement.setString(3, ip);
                statement.setString(4, joined);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not create player data: " + e.getMessage());
            }
        }
    }

    public void updatePlayerData(PlayerData playerData) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET ips = ? WHERE uuid = ?")) {
            statement.setString(1, String.join(",", playerData.getIps()));
            statement.setString(2, playerData.getUuid().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update player data: " + e.getMessage());
        }
    }


    public List<PlayerData> getAllPlayerData() {
        List<PlayerData> playerDataList = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM player_data");
            while (rs.next()) {
                playerDataList.add(new PlayerData(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        new ArrayList<>(Arrays.asList(rs.getString("ips").split(","))),
                        rs.getString("joined")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve all player data: " + e.getMessage());
        }
        return playerDataList;
    }
}