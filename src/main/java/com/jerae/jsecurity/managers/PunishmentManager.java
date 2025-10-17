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
        databaseManager.executeAsync(() -> {
            try (Connection connection = databaseManager.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO punishments (uuid, type, reason, staff_name, end_time) VALUES (?, ?, ?, ?, ?)")) {
                    statement.setString(1, ban.getUuid().toString());
                    statement.setString(2, "ban");
                    statement.setString(3, ban.getReason());
                    statement.setString(4, ban.getStaffName());
                    statement.setLong(5, ban.getExpiration());
                    statement.executeUpdate();
                }
                if (ban.getIpAddress() != null) {
                    try (PreparedStatement statement = connection.prepareStatement("INSERT INTO ip_bans (ip_address, uuid) VALUES (?, ?)")) {
                        statement.setString(1, ban.getIpAddress());
                        statement.setString(2, ban.getUuid().toString());
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not add ban: " + e.getMessage());
            }
        });
    }

    public void removeBan(UUID uuid) {
        databaseManager.executeAsync(() -> {
            try (Connection connection = databaseManager.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM punishments WHERE uuid = ? AND type = 'ban'")) {
                    statement.setString(1, uuid.toString());
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM ip_bans WHERE uuid = ?")) {
                    statement.setString(1, uuid.toString());
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not remove ban: " + e.getMessage());
            }
        });
    }

    public BanEntry getBan(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT p.*, pd.name, pd.ips FROM punishments p JOIN player_data pd ON p.uuid = pd.uuid WHERE p.uuid = ? AND p.type = 'ban'")) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                BanEntry ban = new BanEntry(
                        uuid,
                        rs.getString("name"),
                        rs.getString("ips"),
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
        databaseManager.executeAsync(() -> {
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
        });
    }

    public void removeMute(UUID uuid) {
        databaseManager.executeAsync(() -> {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM punishments WHERE uuid = ? AND type = 'mute'")) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not remove mute: " + e.getMessage());
            }
        });
    }

    public MuteEntry getMute(UUID uuid) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT p.*, pd.name FROM punishments p JOIN player_data pd ON p.uuid = pd.uuid WHERE p.uuid = ? AND p.type = 'mute'")) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                MuteEntry mute = new MuteEntry(
                        uuid,
                        rs.getString("name"),
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
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT uuid FROM ip_bans WHERE ip_address = ?")) {
            statement.setString(1, ipAddress);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return getBan(UUID.fromString(rs.getString("uuid")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve ban by IP: " + e.getMessage());
        }
        return null;
    }

    public boolean isIpBanned(String ipAddress) {
        return getBanByIp(ipAddress) != null;
    }

    public List<PunishmentLogEntry> getPunishmentLogs() {
        List<PunishmentLogEntry> logs = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT p.*, pd.name FROM punishments p JOIN player_data pd ON p.uuid = pd.uuid ORDER BY p.id DESC")) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                logs.add(new PunishmentLogEntry(
                        rs.getString("name"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("type"),
                        rs.getString("reason"),
                        rs.getString("staff_name"),
                        rs.getLong("end_time")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve punishment logs: " + e.getMessage());
        }
        return logs;
    }

    public List<PunishmentLogEntry> getPlayerHistory(UUID playerUUID) {
        List<PunishmentLogEntry> logs = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT p.*, pd.name FROM punishments p JOIN player_data pd ON p.uuid = pd.uuid WHERE p.uuid = ? ORDER BY p.id DESC")) {
            statement.setString(1, playerUUID.toString());
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                logs.add(new PunishmentLogEntry(
                        rs.getString("name"),
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("type"),
                        rs.getString("reason"),
                        rs.getString("staff_name"),
                        rs.getLong("end_time")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve player history: " + e.getMessage());
        }
        return logs;
    }

    public java.util.Collection<BanEntry> getBannedPlayers() {
        List<BanEntry> bans = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT p.*, pd.name, pd.ips FROM punishments p JOIN player_data pd ON p.uuid = pd.uuid WHERE p.type = 'ban'")) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                BanEntry ban = new BanEntry(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        rs.getString("ips"),
                        rs.getString("reason"),
                        rs.getString("staff_name"),
                        rs.getLong("end_time")
                );
                if (!ban.hasExpired()) {
                    bans.add(ban);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve banned players: " + e.getMessage());
        }
        return bans;
    }

    public java.util.Collection<MuteEntry> getMutedPlayers() {
        List<MuteEntry> mutes = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT p.*, pd.name FROM punishments p JOIN player_data pd ON p.uuid = pd.uuid WHERE p.type = 'mute'")) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                MuteEntry mute = new MuteEntry(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("name"),
                        rs.getString("reason"),
                        rs.getString("staff_name"),
                        rs.getLong("end_time")
                );
                if (!mute.hasExpired()) {
                    mutes.add(mute);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve muted players: " + e.getMessage());
        }
        return mutes;
    }

    public void addWarn(WarnEntry warn) {
        databaseManager.executeAsync(() -> {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO punishments (uuid, type, reason, staff_name, end_time) VALUES (?, ?, ?, ?, ?)")) {
                statement.setString(1, warn.getUuid().toString());
                statement.setString(2, "warn");
                statement.setString(3, warn.getReason());
                statement.setString(4, warn.getStaffName());
                statement.setLong(5, -1);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not add warn: " + e.getMessage());
            }
        });
    }
}