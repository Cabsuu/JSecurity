package com.jerae.jsecurity.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.models.PlayerData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDataManager {
    private final JSecurity plugin;
    private final File dataFile;
    private final List<PlayerData> playerDataList;
    private final Gson gson;

    public PlayerDataManager(JSecurity plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "player_data.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playerDataList = loadPlayerData();
    }

    private List<PlayerData> loadPlayerData() {
        if (!dataFile.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<List<PlayerData>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load player data: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void savePlayerData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(playerDataList, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data: " + e.getMessage());
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        for (PlayerData data : playerDataList) {
            if (data.getUuid().equals(uuid)) {
                return data;
            }
        }
        return null;
    }

    public PlayerData getPlayerData(String name) {
        for (PlayerData data : playerDataList) {
            if (data.getName().equalsIgnoreCase(name)) {
                return data;
            }
        }
        return null;
    }

    public void createPlayerData(UUID uuid, String name, String ip) {
        if (getPlayerData(uuid) == null) {
            int id = playerDataList.size() + 1;
            String joined = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm").format(java.time.LocalDateTime.now());
            List<String> ips = new ArrayList<>();
            ips.add(ip);
            PlayerData newPlayerData = new PlayerData(id, uuid, name, ips, joined);
            playerDataList.add(newPlayerData);
            savePlayerData();
        }
    }

    public List<PlayerData> getAllPlayerData() {
        return new ArrayList<>(playerDataList);
    }
}