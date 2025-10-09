package com.jerae.jsecurity.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jerae.jsecurity.models.PunishmentLogEntry;
import com.jerae.jsecurity.models.WarnEntry;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PunishmentManager {

    private final JavaPlugin plugin;
    private final File bansFile;
    private final File mutesFile;
    private final File punishmentLogFile;
    private final Gson gson;

    private Map<UUID, BanEntry> bans = new ConcurrentHashMap<>();
    private Map<UUID, MuteEntry> mutes = new ConcurrentHashMap<>();
    private Map<String, UUID> ipBans = new ConcurrentHashMap<>();
    private List<PunishmentLogEntry> punishmentLog = new ArrayList<>();

    public PunishmentManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.bansFile = new File(plugin.getDataFolder(), "bans.json");
        this.mutesFile = new File(plugin.getDataFolder(), "mutes.json");
        this.punishmentLogFile = new File(plugin.getDataFolder(), "punish-log.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadPunishments();
    }

    private void loadPunishments() {
        plugin.getDataFolder().mkdirs();

        // Load Bans
        if (bansFile.exists()) {
            try (FileReader reader = new FileReader(bansFile)) {
                Type type = new TypeToken<Map<UUID, BanEntry>>() {}.getType();
                bans = gson.fromJson(reader, type);
                if (bans == null) {
                    bans = new ConcurrentHashMap<>();
                }
                bans.values().forEach(ban -> {
                    if (ban.getIpAddress() != null) {
                        ipBans.put(ban.getIpAddress(), ban.getUuid());
                    }
                });
            } catch (IOException e) {
                plugin.getLogger().severe("Could not load bans.json");
                e.printStackTrace();
            }
        }

        // Load Mutes
        if (mutesFile.exists()) {
            try (FileReader reader = new FileReader(mutesFile)) {
                Type type = new TypeToken<Map<UUID, MuteEntry>>() {}.getType();
                mutes = gson.fromJson(reader, type);
                if (mutes == null) {
                    mutes = new ConcurrentHashMap<>();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not load mutes.json");
                e.printStackTrace();
            }
        }

        // Load Punishment Log
        if (punishmentLogFile.exists()) {
            try (FileReader reader = new FileReader(punishmentLogFile)) {
                Type type = new TypeToken<List<PunishmentLogEntry>>() {}.getType();
                punishmentLog = gson.fromJson(reader, type);
                if (punishmentLog == null) {
                    punishmentLog = new ArrayList<>();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not load punish-log.json");
                e.printStackTrace();
            }
        }
    }

    public void savePunishments() {
        // Save Bans
        try (FileWriter writer = new FileWriter(bansFile)) {
            gson.toJson(bans, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save bans to bans.json");
            e.printStackTrace();
        }

        // Save Mutes
        try (FileWriter writer = new FileWriter(mutesFile)) {
            gson.toJson(mutes, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save mutes to mutes.json");
            e.printStackTrace();
        }

        // Save Punishment Log
        try (FileWriter writer = new FileWriter(punishmentLogFile)) {
            gson.toJson(punishmentLog, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save punishment log to punish-log.json");
            e.printStackTrace();
        }
    }

    private void logPunishment(String playerName, UUID playerUUID, String punishmentType, String reason, String staffName) {
        PunishmentLogEntry logEntry = new PunishmentLogEntry(playerName, playerUUID, punishmentType, reason, staffName, System.currentTimeMillis());
        punishmentLog.add(0, logEntry); // Prepend to have newest first
    }

    public void addBan(BanEntry ban) {
        bans.put(ban.getUuid(), ban);
        if (ban.getIpAddress() != null) {
            ipBans.put(ban.getIpAddress(), ban.getUuid());
        }
        String type = ban.isPermanent() ? "Ban" : "Temp-Ban";
        logPunishment(ban.getPlayerName(), ban.getUuid(), type, ban.getReason(), ban.getStaffName());
        savePunishments();
    }

    public void removeBan(UUID uuid) {
        BanEntry ban = bans.remove(uuid);
        if (ban != null && ban.getIpAddress() != null) {
            ipBans.remove(ban.getIpAddress());
        }
        savePunishments();
    }

    public BanEntry getBan(UUID uuid) {
        BanEntry ban = bans.get(uuid);
        if (ban != null && ban.hasExpired()) {
            removeBan(uuid);
            return null;
        }
        return ban;
    }

    public BanEntry getBanByIp(String ipAddress) {
        UUID bannedUUID = ipBans.get(ipAddress);
        return bannedUUID != null ? getBan(bannedUUID) : null;
    }

    public boolean isBanned(UUID uuid) {
        return getBan(uuid) != null;
    }

    public boolean isIpBanned(String ipAddress) {
        return getBanByIp(ipAddress) != null;
    }

    public void addMute(MuteEntry mute) {
        mutes.put(mute.getUuid(), mute);
        String type = mute.isPermanent() ? "Mute" : "Temp-Mute";
        logPunishment(mute.getPlayerName(), mute.getUuid(), type, mute.getReason(), mute.getStaffName());
        savePunishments();
    }

    public void removeMute(UUID uuid) {
        mutes.remove(uuid);
        savePunishments();
    }

    public MuteEntry getMute(UUID uuid) {
        MuteEntry mute = mutes.get(uuid);
        if (mute != null && mute.hasExpired()) {
            removeMute(uuid);
            return null;
        }
        return mute;
    }

    public boolean isMuted(UUID uuid) {
        return getMute(uuid) != null;
    }

    public void addWarn(WarnEntry warn) {
        logPunishment(warn.getPlayerName(), warn.getUuid(), "Warn", warn.getReason(), warn.getStaffName());
        savePunishments();
    }

    public List<PunishmentLogEntry> getPunishmentLogs() {
        return punishmentLog;
    }

    public List<PunishmentLogEntry> getPlayerHistory(UUID playerUUID) {
        return punishmentLog.stream()
                .filter(entry -> entry.getPlayerUUID().equals(playerUUID))
                .collect(Collectors.toList());
    }

    public java.util.Collection<BanEntry> getBannedPlayers() {
        return bans.values();
    }

    public java.util.Collection<MuteEntry> getMutedPlayers() {
        return mutes.values();
    }
}