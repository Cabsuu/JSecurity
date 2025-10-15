package com.jerae.jsecurity.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    private boolean announceNewPlayer;
    private List<Integer> announceMilestones;
    private String reloadMessage;
    private String newPlayerBroadcastMessage;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        loadMessages();
        loadValues();
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

    private void loadValues() {
        announceNewPlayer = config.getBoolean("announce-new-player", true);
        announceMilestones = config.getIntegerList("announce-milestones");
        reloadMessage = messagesConfig.getString("other.reload-message", "&aConfiguration reloaded.");
        newPlayerBroadcastMessage = messagesConfig.getString("other.new-player-broadcast", "&aWelcome our {player_count}th player!");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadMessages();
        loadValues();
    }

    public String getString(String path) {
        return config.getString(path, "");
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path, false);
    }

    public String getDefaultBanReason() {
        return messagesConfig.getString("default-reasons.ban", "You have been banned.");
    }

    public String getDefaultMuteReason() {
        return messagesConfig.getString("default-reasons.mute", "You have been muted.");
    }

    public String getDefaultKickReason() {
        return messagesConfig.getString("default-reasons.kick", "Kicked by a staff member.");
    }

    public String getDefaultIpbanReason() {
        return messagesConfig.getString("default-reasons.ipban", "IP Banned by a staff member.");
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

    public boolean isAltAccountAlertEnabled() {
        return getBoolean("alt-account-alert");
    }

    public String getMessage(String path, boolean hasReason) {
        String reasonPath = hasReason ? "punishments.with_reason." : "punishments.without_reason.";
        String fullPath = reasonPath + path;
        String message = messagesConfig.getString(fullPath);

        if (message == null || message.isEmpty()) {
            return getMessage(path);
        }
        return message;
    }

    public String getMessage(String path) {
        return messagesConfig.getString("other." + path, "");
    }

    public boolean isAnnounceNewPlayerEnabled() {
        return announceNewPlayer;
    }

    public List<Integer> getAnnounceMilestones() {
        return announceMilestones;
    }

    public String getReloadMessage() {
        return reloadMessage;
    }

    public String getNewPlayerBroadcastMessage() {
        return newPlayerBroadcastMessage;
    }

    public boolean isChatDelayEnabled() {
        return config.getBoolean("chat-delay.enabled", false);
    }

    public double getChatDelay() {
        return config.getDouble("chat-delay.period", 3.0);
    }

    public boolean isKeywordReplacementEnabled() {
        return config.getBoolean("keyword-replacement.enabled", false);
    }

    public java.util.Map<String, String> getKeywordReplacementMap() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        if (isKeywordReplacementEnabled()) {
            for (String key : config.getConfigurationSection("keyword-replacement.words").getKeys(false)) {
                for (String word : config.getStringList("keyword-replacement.words." + key)) {
                    map.put(word.toLowerCase(), key);
                }
            }
        }
        return map;
    }

    public String getNoPermissionMessage() {
        return messagesConfig.getString("other.no-permission", "&cYou do not have permission to use this command.");
    }

    public String getPrivateMessageToSenderFormat() {
        return messagesConfig.getString("other.private-message.to-sender", "&7[&bme &d-> &b{target}&7] &f{content}");
    }

    public String getPrivateMessageToReceiverFormat() {
        return messagesConfig.getString("other.private-message.to-receiver", "&7[&b{sender} &d-> &bme&7] &f{content}");
    }

    public List<String> getMutedCommandRestriction() {
        return config.getStringList("muted-command-restriction");
    }

    public int getMinPasswordLength() {
        return config.getInt("authentication.min-password-length", 8);
    }

    public int getMaxPasswordLength() {
        return config.getInt("authentication.max-password-length", 16);
    }

    public boolean isUppercaseRequired() {
        return config.getBoolean("authentication.password-must-contain.uppercase-letter", false);
    }

    public boolean isNumberRequired() {
        return config.getBoolean("authentication.password-must-contain.number", false);
    }

    public boolean isSymbolRequired() {
        return config.getBoolean("authentication.password-must-contain.symbol", false);
    }
}