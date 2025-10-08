package com.jerae.jsecurity.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        loadMessages();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defMessagesStream = plugin.getResource("messages.yml");
        if (defMessagesStream != null) {
            messagesConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defMessagesStream)));
        }
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadMessages();
    }

    public String getString(String path) {
        return config.getString(path, "");
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }

    public String getDefaultBanReason() {
        return getString("default-reasons.ban");
    }

    public String getDefaultMuteReason() {
        return getString("default-reasons.mute");
    }

    public String getDefaultKickReason() {
        return messagesConfig.getString("default-kick-reason", "Kicked by a staff member.");
    }

    public String getDefaultIpbanReason() {
        return messagesConfig.getString("default-ipban-reason", "IP Banned by a staff member.");
    }

    public String getDefaultTempBanDuration() {
        return getString("default-durations.tempban");
    }

    public String getDefaultTempMuteDuration() {
        return getString("default-durations.tempmute");
    }

    public boolean isBanEvasionPreventionEnabled() {
        return getBoolean("prevent-ban-evasion");
    }

    public String getMessage(String path, boolean hasReason) {
        String reasonPath = hasReason ? "punishments.with_reason." : "without_reason.";
        String fullPath = "punishments." + reasonPath + path;
        String message = messagesConfig.getString(fullPath);

        if (message == null || message.isEmpty()) {
            return getMessage(path);
        }
        return message;
    }

    public String getMessage(String path) {
        return messagesConfig.getString("other." + path, "");
    }
}