package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.utils.PlaceholderAPI;
import com.jerae.jsecurity.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final PunishmentManager punishmentManager;
    private final ConfigManager configManager;

    public PlayerListener(PunishmentManager punishmentManager, ConfigManager configManager) {
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String playerName = event.getName();
        String ipAddress = event.getAddress().getHostAddress();

        // Check for existing ban on the UUID
        BanEntry ban = punishmentManager.getBan(uuid);
        if (ban != null) {
            String kickMessage;
            boolean hasReason = ban.getReason() != null && !ban.getReason().isEmpty() && !ban.getReason().equals(configManager.getDefaultBanReason());

            PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                    .setPlayerName(playerName)
                    .setReason(ban.getReason());

            if (ban.isPermanent()) {
                kickMessage = configManager.getMessage("ban-kick-message", hasReason);
            } else {
                data.setDuration(TimeUtil.formatRemainingTime(ban.getExpiration()));
                kickMessage = configManager.getMessage("tempban-kick-message", hasReason);
            }
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(kickMessage, data)));
            return;
        }

        // Check for ban evasion
        if (configManager.isBanEvasionPreventionEnabled()) {
            BanEntry ipBan = punishmentManager.getBanByIp(ipAddress);
            if (ipBan != null) {
                // The player's IP is banned, but their UUID is not. This is ban evasion.
                String originalBannedPlayer = ipBan.getPlayerName();
                PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData().setBannedPlayer(originalBannedPlayer);

                String kickMessage = configManager.getMessage("ban-evasion-kick-message");
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(kickMessage, data)));

                // Create a new ban entry for the evading account
                String evasionReason = configManager.getMessage("ban-evasion-reason");
                BanEntry evasionBan = new BanEntry(
                        uuid,
                        playerName,
                        ipAddress,
                        PlaceholderAPI.setPlaceholders(evasionReason, data),
                        "jSecurity",
                        ipBan.getExpiration() // Match the original ban's expiration
                );
                punishmentManager.addBan(evasionBan);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ipAddress = player.getAddress().getAddress().getHostAddress();

        if (configManager.isAltAccountAlertEnabled()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer != player && onlinePlayer.getAddress().getAddress().getHostAddress().equals(ipAddress)) {

                    PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                            .setPlayerName(player.getName())
                            .setAltPlayer(onlinePlayer.getName());

                    String alertMessage = configManager.getMessage("alt-account-alert");
                    String formattedMessage = PlaceholderAPI.setPlaceholders(alertMessage, data);

                    Bukkit.getOnlinePlayers().stream()
                            .filter(p -> p.hasPermission("jsecurity.alt.alert"))
                            .forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedMessage)));

                    Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(formattedMessage));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (punishmentManager.isMuted(player.getUniqueId())) {
            event.setCancelled(true);

            var mute = punishmentManager.getMute(player.getUniqueId());
            String muteMessage;
            PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                    .setTarget(player)
                    .setReason(mute.getReason());

            if (mute.isPermanent()) {
                muteMessage = configManager.getMessage("mute-message");
            } else {
                data.setDuration(TimeUtil.formatRemainingTime(mute.getExpiration()));
                muteMessage = configManager.getMessage("tempmute-message");
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(muteMessage, data)));
        }
    }

    public void onPlayerBan(OfflinePlayer bannedPlayer, String ipAddress) {
        if (configManager.isBanEvasionPreventionEnabled() && ipAddress != null) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.getUniqueId().equals(bannedPlayer.getUniqueId()) && onlinePlayer.getAddress().getAddress().getHostAddress().equals(ipAddress)) {

                    PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                            .setBannedPlayer(bannedPlayer.getName());

                    String kickMessage = configManager.getMessage("kick-messages.alt-account-banned");
                    Component kickComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(PlaceholderAPI.setPlaceholders(kickMessage, data));
                    onlinePlayer.kick(kickComponent);
                }
            }
        }
    }
}