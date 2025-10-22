package com.jerae.jsecurity.managers;

import com.jerae.jsecurity.utils.ColorUtil;
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
        reloadMessage = getColorizedMessage("other.reload-message", "&aConfiguration reloaded.");
        newPlayerBroadcastMessage = getColorizedMessage("other.new-player-broadcast", "&aWelcome our {player_count}th player!");
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
        return getColorizedMessage("default-reasons.ban", "You have been banned.");
    }

    public String getDefaultMuteReason() {
        return getColorizedMessage("default-reasons.mute", "You have been muted.");
    }

    public String getDefaultKickReason() {
        return getColorizedMessage("default-reasons.kick", "Kicked by a staff member.");
    }

    public String getDefaultIpbanReason() {
        return getColorizedMessage("default-reasons.ipban", "IP Banned by a staff member.");
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
        String message = getColorizedMessage(fullPath, "");

        if (message.isEmpty()) {
            return getMessage(path);
        }
        return message;
    }

    public String getMessage(String path) {
        return getColorizedMessage("other." + path, "");
    }

    private String getColorizedMessage(String path, String defaultValue) {
        return ColorUtil.colorize(messagesConfig.getString(path, defaultValue));
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

    public String getChatDelayMessage() {
        return getColorizedMessage("other.chat-delay-message", "&cYou must wait {time} seconds before chatting again.");
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
        return getColorizedMessage("other.no-permission", "&cYou do not have permission to use this command.");
    }

    public String getPrivateMessageToSenderFormat() {
        return getColorizedMessage("other.private-message.to-sender", "&7[&bme &d-> &b{target}&7] &f{content}");
    }

    public String getPrivateMessageToReceiverFormat() {
        return getColorizedMessage("other.private-message.to-receiver", "&7[&b{sender} &d-> &bme&7] &f{content}");
    }

    public String getStaffChatMessageFormat() {
        return getColorizedMessage("other.staff-chat-format", "&8[&cStaffChat&8] &7{player}: {message}");
    }

    public String getStaffChatUsageMessage() {
        return getColorizedMessage("other.staff-chat-usage", "&cUsage: /staffchat <message> or /sc <message>");
    }

    public String getStaffChatToggleMessage(boolean on) {
        return on ? getColorizedMessage("other.staff-chat-toggle-on", "&aStaff chat toggled on.") : getColorizedMessage("other.staff-chat-toggle-off", "&cStaff chat toggled off.");
    }

    public String getPlayerOnlyCommandMessage() {
        return getColorizedMessage("other.player-only-command", "&cThis command can only be used by players.");
    }

    public String getPlayerNotFoundMessage() {
        return getColorizedMessage("other.player-not-found", "&cPlayer not found.");
    }

    public String getInvalidPageNumberMessage() {
        return getColorizedMessage("other.invalid-page-number", "&cInvalid page number.");
    }

    public String getPrivateMessageUsageMessage() {
        return getColorizedMessage("other.private-message.usage", "&cUsage: /message <player> <message>");
    }

    public String getReplyUsageMessage() {
        return getColorizedMessage("other.reply.usage", "&cUsage: /reply <message>");
    }

    public String getNoOneToReplyToMessage() {
        return getColorizedMessage("other.reply.no-one-to-reply-to", "&cYou have no one to reply to.");
    }

    public String getSocialSpyEnabledMessage() {
        return getColorizedMessage("other.social-spy.enabled", "&aSocial spy enabled.");
    }

    public String getSocialSpyDisabledMessage() {
        return getColorizedMessage("other.social-spy.disabled", "&cSocial spy disabled.");
    }

    public String getVanishMessage() {
        return getColorizedMessage("other.vanish.vanished", "&aYou are now vanished.");
    }

    public String getUnvanishMessage() {
        return getColorizedMessage("other.vanish.unvanished", "&aYou are no longer vanished.");
    }

    public String getAlreadyFrozenMessage(String player) {
        return getColorizedMessage("other.freeze.already-frozen", "&c{player} is already frozen.").replace("{player}", player);
    }

    public String getNotFrozenMessage(String player) {
        return getColorizedMessage("other.freeze.not-frozen", "&c{player} is not frozen.").replace("{player}", player);
    }

    public String getFrozenMessage(String player) {
        return getColorizedMessage("other.freeze.frozen", "&aYou have frozen {player}.").replace("{player}", player);
    }

    public String getUnfrozenMessage(String player) {
        return getColorizedMessage("other.freeze.unfrozen", "&aYou have unfrozen {player}.").replace("{player}", player);
    }

    public String getFreezeUsageMessage() {
        return getColorizedMessage("other.freeze.usage-freeze", "&cUsage: /freeze <player>");
    }

    public String getUnfreezeUsageMessage() {
        return getColorizedMessage("other.freeze.usage-unfreeze", "&cUsage: /unfreeze <player>");
    }

    public String getJsecurityRecordHeader(int page, int totalPages) {
        return getColorizedMessage("other.jsecurity.record.header", "&6--- Player Records (Page {page}/{totalPages}) ---")
                .replace("{page}", String.valueOf(page))
                .replace("{totalPages}", String.valueOf(totalPages));
    }

    public String getJsecurityRecordFormat(int id, String name) {
        return getColorizedMessage("other.jsecurity.record.format", "&e{id}. &f{name}")
                .replace("{id}", String.valueOf(id))
                .replace("{name}", name);
    }

    public String getJsecurityProfileUsageMessage() {
        return getColorizedMessage("other.jsecurity.profile.usage", "&cUsage: /js profile <player>");
    }

    public String getJsecurityProfileHeader(String player) {
        return getColorizedMessage("other.jsecurity.profile.header", "&6--- Player Profile: {player} ---").replace("{player}", player);
    }

    public String getJsecurityProfileId(int id) {
        return getColorizedMessage("other.jsecurity.profile.id", "&eID: &f{id}").replace("{id}", String.valueOf(id));
    }

    public String getJsecurityProfileUuid(String uuid) {
        return getColorizedMessage("other.jsecurity.profile.uuid", "&eUUID: &f{uuid}").replace("{uuid}", uuid);
    }

    public String getJsecurityProfileLastIp(String ip) {
        return getColorizedMessage("other.jsecurity.profile.last-ip", "&eLast IP: &f{ip}").replace("{ip}", ip);
    }

    public String getJsecurityProfileFirstJoined(String date) {
        return getColorizedMessage("other.jsecurity.profile.first-joined", "&eFirst Joined: &f{date}").replace("{date}", date);
    }

    public String getJsecurityProfileStatus(String status) {
        return getColorizedMessage("other.jsecurity.profile.status", "&eStatus: {status}").replace("{status}", status);
    }

    public String getJsecurityProfileBannedReason(String reason) {
        return getColorizedMessage("other.jsecurity.profile.banned-reason", "&eReason: &f{reason}").replace("{reason}", reason);
    }

    public String getJsecurityProfileNotesHeader() {
        return getColorizedMessage("other.jsecurity.profile.notes", "&eNotes:");
    }

    public String getJsecurityProfileNoteFormat(String note) {
        return getColorizedMessage("other.jsecurity.profile.note-format", "&f- {note}").replace("{note}", note);
    }

    public String getJsecurityLogNoLogsMessage() {
        return getColorizedMessage("other.jsecurity.log.no-logs", "&eThere are no punishment logs.");
    }

    public String getJsecurityLogHeader(int page, int totalPages) {
        return getColorizedMessage("other.jsecurity.log.header", "&6--- Punishment Log (Page {page}/{totalPages}) ---")
                .replace("{page}", String.valueOf(page))
                .replace("{totalPages}", String.valueOf(totalPages));
    }

    public String getJsecurityLogFormat(String date, String player, String type, String reason) {
        return getColorizedMessage("other.jsecurity.log.format", "&7[{date}] &e{player} - {type} - {reason}")
                .replace("{date}", date)
                .replace("{player}", player)
                .replace("{type}", type)
                .replace("{reason}", reason);
    }

    public String getJsecurityHistoryUsageMessage() {
        return getColorizedMessage("other.jsecurity.history.usage", "&cUsage: /js history <player> [page]");
    }

    public String getJsecurityHistoryNoHistoryMessage() {
        return getColorizedMessage("other.jsecurity.history.no-history", "&eThis player has no punishment history.");
    }

    public String getJsecurityHistoryHeader(String player, int page, int totalPages) {
        return getColorizedMessage("other.jsecurity.history.header", "&6--- History for {player} (Page {page}/{totalPages}) ---")
                .replace("{player}", player)
                .replace("{page}", String.valueOf(page))
                .replace("{totalPages}", String.valueOf(totalPages));
    }

    public String getJsecurityHistoryFormat(String date, String type, String reason) {
        return getColorizedMessage("other.jsecurity.history.format", "&7[{date}] &e{type} - {reason}")
                .replace("{date}", date)
                .replace("{type}", type)
                .replace("{reason}", reason);
    }

    public String getJsecurityNoteUsageMessage() {
        return getColorizedMessage("other.jsecurity.note.usage", "&cUsage: /js note <player> <note|-clear>");
    }

    public String getJsecurityNoteClearedMessage(String player) {
        return getColorizedMessage("other.jsecurity.note.cleared", "&aNotes cleared for {player}.").replace("{player}", player);
    }

    public String getJsecurityNoteAddedMessage(String player) {
        return getColorizedMessage("other.jsecurity.note.added", "&aNote added to {player}'s profile.").replace("{player}", player);
    }

    public String getJsecurityUnregisterUsageMessage() {
        return getColorizedMessage("other.jsecurity.unregister.usage", "&cUsage: /js unregister <player>");
    }

    public String getJsecurityUnregisterNotRegisteredMessage() {
        return getColorizedMessage("other.jsecurity.unregister.not-registered", "&cThat player is not registered.");
    }

    public String getJsecurityUnregisterUnregisteredMessage(String player) {
        return getColorizedMessage("other.jsecurity.unregister.unregistered", "&a{player} has been unregistered.").replace("{player}", player);
    }

    public String getJsecurityUnregisterConfirmMessage(String player) {
        return getColorizedMessage("other.jsecurity.unregister.confirm", "&eAre you sure you want to unregister {player}? This action cannot be undone. Re-enter the command to confirm.").replace("{player}", player);
    }

    public String getJsecurityHelpHeader() {
        return getColorizedMessage("other.jsecurity.help.header", "&6--- JSecurity Help ---");
    }

    public String getJsecurityHelpHelpMessage() {
        return getColorizedMessage("other.jsecurity.help.help", "&e/js help &7- Shows this help message.");
    }

    public String getJsecurityHelpReloadMessage() {
        return getColorizedMessage("other.jsecurity.help.reload", "&e/js reload &7- Reloads the configuration.");
    }

    public String getJsecurityHelpRecordMessage() {
        return getColorizedMessage("other.jsecurity.help.record", "&e/js record [page] [-sort] &7- Shows player records.");
    }

    public String getJsecurityHelpProfileMessage() {
        return getColorizedMessage("other.jsecurity.help.profile", "&e/js profile <player> &7- Shows a player's profile.");
    }

    public String getJsecurityHelpLogMessage() {
        return getColorizedMessage("other.jsecurity.help.log", "&e/js log [page] &7- Shows the punishment log.");
    }

    public String getJsecurityHelpHistoryMessage() {
        return getColorizedMessage("other.jsecurity.help.history", "&e/js history <player> [page] &7- Shows a player's punishment history.");
    }

    public String getJsecurityHelpNoteMessage() {
        return getColorizedMessage("other.jsecurity.help.note", "&e/js note <player> <note> &7- Adds a note to a player's profile.");
    }

    public String getAuthDisabledMessage() {
        return getColorizedMessage("other.authentication.disabled", "&cThe authentication system is disabled.");
    }

    public String getAlreadyLoggedInMessage() {
        return getColorizedMessage("other.authentication.already-logged-in", "&cYou are already logged in.");
    }

    public String getNotRegisteredMessage() {
        return getColorizedMessage("other.authentication.not-registered", "&cYou are not registered. Please register using /register <password> <confirmPassword>");
    }

    public String getLoginUsageMessage() {
        return getColorizedMessage("other.authentication.login.usage", "&cUsage: /login <password>");
    }

    public String getLoginSuccessMessage() {
        return getColorizedMessage("other.authentication.login.success", "&aYou have logged in successfully.");
    }

    public String getLoginFailMessage() {
        return getColorizedMessage("other.authentication.login.fail", "&cIncorrect password.");
    }

    public String getRegisterUsageMessage() {
        return getColorizedMessage("other.authentication.register.usage", "&cUsage: /register <password> <confirmPassword>");
    }

    public String getAlreadyRegisteredMessage() {
        return getColorizedMessage("other.authentication.register.already-registered", "&cYou are already registered.");
    }

    public String getPasswordMismatchMessage() {
        return getColorizedMessage("other.authentication.register.password-mismatch", "&cPasswords do not match.");
    }

    public String getRegisterSuccessMessage() {
        return getColorizedMessage("other.authentication.register.success", "&aYou have been registered successfully. Please log in using /login <password>");
    }

    public String getChangePassUsageMessage() {
        return getColorizedMessage("other.authentication.changepass.usage", "&cUsage: /changepass <oldPassword> <newPassword>");
    }

    public String getChangePassSuccessMessage() {
        return getColorizedMessage("other.authentication.changepass.success", "&aYour password has been changed successfully.");
    }

    public String getChangePassFailMessage() {
        return getColorizedMessage("other.authentication.changepass.fail", "&cIncorrect old password.");
    }

    public String getUnregisterUsageMessage() {
        return getColorizedMessage("other.authentication.unregister.usage", "&cUsage: /unregister <password>");
    }

    public String getUnregisterSuccessMessage() {
        return getColorizedMessage("other.authentication.unregister.success", "&aYou have been unregistered successfully.");
    }

    public String getUnregisterFailMessage() {
        return getColorizedMessage("other.authentication.unregister.fail", "&cIncorrect password.");
    }

    public String getUnregisterConfirmMessage() {
        return getColorizedMessage("other.authentication.unregister.confirm", "&eAre you sure you want to unregister? This action cannot be undone. Re-enter the command within 60 seconds to confirm.");
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

    public boolean isAuthEnabled() {
        return config.getBoolean("authentication.enabled", false);
    }

    public boolean isJoinAtSpawn() {
        return config.getBoolean("authentication.join-at-spawn", true);
    }

    public boolean isReturnAtLocation() {
        return config.getBoolean("authentication.return-at-location", false);
    }

    public boolean isJoinOnBlind() {
        return config.getBoolean("authentication.join-on-blind", false);
    }

    public boolean isJoinOnSpectator() {
        return config.getBoolean("authentication.join-on-spectator", false);
    }

    public boolean isSessionReconnection() {
        return config.getBoolean("authentication.session-reconnection", false);
    }

    public String getSessionLimitTimer() {
        return config.getString("authentication.session-limit-timer", "6h");
    }

    public String getInvseeTitle() {
        return getColorizedMessage("invsee.invsee-title", "%player_name%''s Inventory");
    }

    public String getEnderChestTitle() {
        return getColorizedMessage("invsee.enderchest-title", "%player_name%''s Ender Chest");
    }

    public String getClearButtonName() {
        return getColorizedMessage("invsee.clear-button-name", "&cClear Inventory");
    }

    public String getEnderChestButtonName() {
        return getColorizedMessage("invsee.enderchest-button-name", "&5Ender Chest");
    }

    public String getTeleportButtonName() {
        return getColorizedMessage("invsee.teleport-button-name", "&aTeleport to Player");
    }

    public String getCloseButtonName() {
        return getColorizedMessage("invsee.close-button-name", "&cClose");
    }

    public String getBackButtonName() {
        return getColorizedMessage("invsee.back-button-name", "&cBack to Inventory");
    }

    public String getInvseeUsageMessage() {
        return getColorizedMessage("invsee.usage", "&cUsage: /invsee <player>");
    }

    public String getTeleportSuccessMessage() {
        return getColorizedMessage("invsee.teleport-success", "&aSuccessfully teleported to %player_name%.");
    }

    public String getInventoryClearedMessage() {
        return getColorizedMessage("invsee.inventory-cleared", "&aSuccessfully cleared %player_name%''s inventory.");
    }
}