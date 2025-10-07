package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.utils.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerListener implements Listener {

    private final PunishmentManager punishmentManager;
    private final ConfigManager configManager;

    public PlayerListener(PunishmentManager punishmentManager, ConfigManager configManager) {
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String ipAddress = event.getAddress().getHostAddress();

        // Check for existing ban on the UUID
        BanEntry ban = punishmentManager.getBan(player.getUniqueId());
        if (ban != null) {
            String kickMessage;
            if (ban.isPermanent()) {
                kickMessage = configManager.getMessage("kick-messages.ban")
                        .replace("{reason}", ban.getReason());
            } else {
                kickMessage = configManager.getMessage("kick-messages.tempban")
                        .replace("{reason}", ban.getReason())
                        .replace("{duration}", TimeUtil.formatRemainingTime(ban.getExpiration()));
            }
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', kickMessage));
            return;
        }

        // Check for ban evasion
        if (configManager.isBanEvasionPreventionEnabled()) {
            BanEntry ipBan = punishmentManager.getBanByIp(ipAddress);
            if (ipBan != null) {
                // The player's IP is banned, but their UUID is not. This is ban evasion.
                String kickMessage = configManager.getMessage("kick-messages.ban-evasion");
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', kickMessage));

                // Create a new ban entry for the evading account
                BanEntry evasionBan = new BanEntry(
                        player.getUniqueId(),
                        player.getName(),
                        ipAddress,
                        "Ban Evasion",
                        "jSecurity",
                        ipBan.getExpiration() // Match the original ban's expiration
                );
                punishmentManager.addBan(evasionBan);
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
}