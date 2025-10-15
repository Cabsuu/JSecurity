package com.jerae.jsecurity.managers;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.models.PunishmentLogEntry;
import com.jerae.jsecurity.models.WarnEntry;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PunishmentManager {

    private final JSecurity plugin;
    private final DatabaseManager databaseManager;

    public PunishmentManager(JSecurity plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void addBan(BanEntry ban) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO punishments (uuid, type, reason, staff_name, end_time) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, ban.getUuid().toString());
            statement.setString(2, "ban");
            statement.setString(3, ban.getReason());
            statement.setString(4, ban.getStaffName());
            statement.setLong(5, ban.getExpiration());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not add ban: " + e.getMessage());
        }
    }

    public void removeBan(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM punishments WHERE uuid = ? AND type = 'ban'")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not remove ban: " + e.getMessage());
        }
    }

    public BanEntry getBan(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM punishments WHERE uuid = ? AND type = 'ban'")) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                BanEntry ban = new BanEntry(
                        uuid,
                        null,
                        null,
                        rs.getString("reason"),
                        rs.getString("staff_name"),
                        rs.getLong("end_time")
                );
                if (ban.hasExpired()) {
                    removeBan(uuid);
                    return null;
                }
                return ban;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve ban: " + e.getMessage());
        }
        return null;
    }

    public void addMute(MuteEntry mute) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO punishments (uuid, type, reason, staff_name, end_time) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, mute.getUuid().toString());
            statement.setString(2, "mute");
            statement.setString(3, mute.getReason());
            statement.setString(4, mute.getStaffName());
            statement.setLong(5, mute.getExpiration());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not add mute: " + e.getMessage());
        }
    }

    public void removeMute(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM punishments WHERE uuid = ? AND type = 'mute'")) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not remove mute: " + e.getMessage());
        }
    }

    public MuteEntry getMute(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM punishments WHERE uuid = ? AND type = 'mute'")) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                MuteEntry mute = new MuteEntry(
                        uuid,
                        null,
                        rs.getString("reason"),
                        rs.getString("staff_name"),
                        rs.getLong("end_time")
                );
                if (mute.hasExpired()) {
                    removeMute(uuid);
                    return null;
                }
                return mute;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve mute: " + e.getMessage());
        }
        return null;
    }

    public boolean isBanned(UUID uuid) {
        return getBan(uuid) != null;
    }

    public boolean isMuted(UUID uuid) {
        return getMute(uuid) != null;
    }

    public BanEntry getBanByIp(String ipAddress) {
        // This method requires changes to the database schema and is not implemented in this refactor.
        return null;
    }

    public boolean isIpBanned(String ipAddress) {
        // This method requires changes to the database schema and is not implemented in this refactor.
        return false;
    }

    public List<PunishmentLogEntry> getPunishmentLogs() {
        // This method requires changes to the database schema and is not implemented in this refactor.
        return new ArrayList<>();
    }

    public List<PunishmentLogEntry> getPlayerHistory(UUID playerUUID) {
        // This method requires changes to the database schema and is not implemented in this refactor.
        return new ArrayList<>();
    }

    public java.util.Collection<BanEntry> getBannedPlayers() {
        // This method requires changes to the database schema and is not implemented in this refactor.
        return new ArrayList<>();
    }

    public java.util.Collection<MuteEntry> getMutedPlayers() {
        // This method requires changes to the database schema and is not implemented in this refactor.
        return new ArrayList<>();
    }

    public void addWarn(WarnEntry warn) {
        // This method requires changes to the database schema and is not implemented in this refactor.
    }
}