package com.jerae.jsecurity.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentManager {

    private final JavaPlugin plugin;
    private final File bansFile;
    private final File mutesFile;
    private final Gson gson;

    private Map<UUID, BanEntry> bans = new ConcurrentHashMap<>();
    private Map<UUID, MuteEntry> mutes = new ConcurrentHashMap<>();
    private Map<String, UUID> ipBans = new ConcurrentHashMap<>();

    public PunishmentManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.bansFile = new File(plugin.getDataFolder(), "bans.json");
        this.mutesFile = new File(plugin.getDataFolder(), "mutes.json");
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
    }

    public void savePunishments() {
        // Save Bans
        try (FileWriter writer = new FileWriter(bansFile)) {
            gson.toJson(bans, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save bans.to bans.json");
            e.printStackTrace();
        }

        // Save Mutes
        try (FileWriter writer = new FileWriter(mutesFile)) {
            gson.toJson(mutes, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save mutes to mutes.json");
            e.printStackTrace();
        }
    }

    public void addBan(BanEntry ban) {
        bans.put(ban.getUuid(), ban);
        if (ban.getIpAddress() != null) {
            ipBans.put(ban.getIpAddress(), ban.getUuid());
        }
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

    public java.util.Collection<BanEntry> getBannedPlayers() {
        return bans.values();
    }

    public java.util.Collection<MuteEntry> getMutedPlayers() {
        return mutes.values();
    }
}