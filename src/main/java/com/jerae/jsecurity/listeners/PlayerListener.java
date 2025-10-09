package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            if (ban.isPermanent()) {
                kickMessage = configManager.getMessage("ban-kick-message", hasReason)
                        .replace("{reason}", ban.getReason());
            } else {
                kickMessage = configManager.getMessage("tempban-kick-message", hasReason)
                        .replace("{reason}", ban.getReason())
                        .replace("{duration}", TimeUtil.formatRemainingTime(ban.getExpiration()));
            }
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', kickMessage));
            return;
        }

        // Check for ban evasion
        if (configManager.isBanEvasionPreventionEnabled()) {
            BanEntry ipBan = punishmentManager.getBanByIp(ipAddress);
            if (ipBan != null) {
                // The player's IP is banned, but their UUID is not. This is ban evasion.
                String originalBannedPlayer = ipBan.getPlayerName();
                String kickMessage = configManager.getMessage("ban-evasion-kick-message")
                        .replace("{banned_player}", originalBannedPlayer);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', kickMessage));

                // Create a new ban entry for the evading account
                String evasionReason = configManager.getMessage("ban-evasion-reason")
                        .replace("{banned_player}", originalBannedPlayer);
                BanEntry evasionBan = new BanEntry(
                        uuid,
                        playerName,
                        ipAddress,
                        evasionReason,
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
                    String alertMessage = configManager.getMessage("alt-account-alert")
                            .replace("{player}", player.getName())
                            .replace("{alt_player}", onlinePlayer.getName());
                    Bukkit.getOnlinePlayers().stream()
                            .filter(p -> p.hasPermission("jsecurity.alt.alert"))
                            .forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', alertMessage)));
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
            if (mute.isPermanent()) {
                muteMessage = configManager.getMessage("mute-message")
                        .replace("{reason}", mute.getReason());
            } else {
                muteMessage = configManager.getMessage("tempmute-message")
                        .replace("{reason}", mute.getReason())
                        .replace("{duration}", TimeUtil.formatRemainingTime(mute.getExpiration()));
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', muteMessage));
        }
    }

    public void onPlayerBan(OfflinePlayer bannedPlayer, String ipAddress) {
        if (configManager.isBanEvasionPreventionEnabled() && ipAddress != null) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.getUniqueId().equals(bannedPlayer.getUniqueId()) && onlinePlayer.getAddress().getAddress().getHostAddress().equals(ipAddress)) {
                    String kickMessage = configManager.getMessage("kick-messages.alt-account-banned")
                            .replace("{banned_player}", bannedPlayer.getName());
                    Component kickComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(kickMessage);
                    onlinePlayer.kick(kickComponent);
                }
            }
        }
    }
}