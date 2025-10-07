package com.jerae.jsecurity.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        // This saves the default config.yml from the jar if it doesn't exist
        plugin.saveDefaultConfig();
        // This gets the config, creating it if it doesn't exist.
        config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
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

    public String getDefaultTempBanDuration() {
        return getString("default-durations.tempban");
    }

    public String getDefaultTempMuteDuration() {
        return getString("default-durations.tempmute");
    }

    public boolean isBanEvasionPreventionEnabled() {
        return getBoolean("prevent-ban-evasion");
    }

    public String getMessage(String path) {
        return getString("messages." + path);
    }
}