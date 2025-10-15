package com.jerae.jsecurity.managers;

import com.jerae.jsecurity.JSecurity;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuthManager {

    private final JSecurity plugin;
    private final Set<UUID> loggedInPlayers = new HashSet<>();
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;

    public AuthManager(JSecurity plugin, ConfigManager configManager, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
    }

    public boolean isRegistered(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT password FROM player_data WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            return rs.next() && rs.getString("password") != null;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if player is registered: " + e.getMessage());
        }
        return false;
    }

    public void registerPlayer(UUID uuid, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET password = ? WHERE uuid = ?")) {
            statement.setString(1, hashedPassword);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not register player: " + e.getMessage());
        }
    }

    public boolean checkPassword(UUID uuid, String password) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT password FROM player_data WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                return hashedPassword != null && BCrypt.checkpw(password, hashedPassword);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check password: " + e.getMessage());
        }
        return false;
    }

    public void loginPlayer(Player player) {
        loggedInPlayers.add(player.getUniqueId());
    }

    public void logoutPlayer(Player player) {
        loggedInPlayers.remove(player.getUniqueId());
    }

    public boolean isLoggedIn(Player player) {
        return loggedInPlayers.contains(player.getUniqueId());
    }

    public void unregisterPlayer(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET password = NULL WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not unregister player: " + e.getMessage());
        }
    }

    public void changePassword(UUID uuid, String newPassword) {
        registerPlayer(uuid, newPassword);
    }

    public String validatePassword(String password) {
        int minLength = configManager.getMinPasswordLength();
        int maxLength = configManager.getMaxPasswordLength();
        boolean uppercaseRequired = configManager.isUppercaseRequired();
        boolean numberRequired = configManager.isNumberRequired();
        boolean symbolRequired = configManager.isSymbolRequired();

        if (password.length() < minLength) {
            return "Password must be at least " + minLength + " characters long.";
        }

        if (password.length() > maxLength) {
            return "Password cannot be more than " + maxLength + " characters long.";
        }

        StringBuilder requirements = new StringBuilder();
        if (uppercaseRequired && !password.matches(".*[A-Z].*")) {
            requirements.append("an uppercase letter, ");
        }

        if (numberRequired && !password.matches(".*\\d.*")) {
            requirements.append("a number, ");
        }

        if (symbolRequired && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            requirements.append("a symbol, ");
        }

        if (!requirements.isEmpty()) {
            requirements.setLength(requirements.length() - 2);
            return "Password must contain " + requirements.toString() + ".";
        }

        return null;
    }
}