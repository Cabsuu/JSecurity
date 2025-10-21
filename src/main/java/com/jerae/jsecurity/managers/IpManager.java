package com.jerae.jsecurity.managers;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.models.PlayerData;
import com.jerae.jsecurity.utils.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class IpManager {

    private final JSecurity plugin;
    private final PlayerDataManager playerDataManager;
    private final PunishmentManager punishmentManager;
    private final ConfigManager configManager;

    public IpManager(JSecurity plugin, PlayerDataManager playerDataManager, PunishmentManager punishmentManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
    }

    public void handlePlayerIp(Player player) {
        String ipAddress = player.getAddress().getAddress().getHostAddress();
        logPlayerIp(player, ipAddress);

        if (configManager.isBanEvasionPreventionEnabled()) {
            checkBanEvasion(player, ipAddress);
        }

        if (configManager.isAltAccountAlertEnabled()) {
            checkAltAccounts(player, ipAddress);
        }
    }

    private void logPlayerIp(Player player, String ipAddress) {
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData != null) {
            if (!playerData.getIps().contains(ipAddress)) {
                playerData.getIps().add(ipAddress);
                playerDataManager.updatePlayerData(playerData);
            }
        } else {
            playerDataManager.createPlayerData(player.getUniqueId(), player.getName(), ipAddress);
        }
    }

    private void checkBanEvasion(Player player, String ipAddress) {
        BanEntry ipBan = punishmentManager.getBanByIp(ipAddress);
        if (ipBan != null) {
            UUID playerUUID = player.getUniqueId();
            if (!ipBan.getUuid().equals(playerUUID)) {
                String originalBannedPlayer = ipBan.getPlayerName();
                PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData().setBannedPlayer(originalBannedPlayer);

                String kickMessage = configManager.getMessage("ban-evasion-kick-message");
                Component kickComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(PlaceholderAPI.setPlaceholders(kickMessage, data));
                player.kick(kickComponent);

                String evasionReason = configManager.getMessage("ban-evasion-reason");
                BanEntry evasionBan = new BanEntry(
                        playerUUID,
                        player.getName(),
                        ipAddress,
                        PlaceholderAPI.setPlaceholders(evasionReason, data),
                        "jSecurity",
                        ipBan.getExpiration()
                );
                punishmentManager.addBan(evasionBan);
            }
        }
    }

    private void checkAltAccounts(Player joiningPlayer, String joiningPlayerIp) {
        Set<String> altAccountNames = new HashSet<>();

        for (PlayerData playerData : playerDataManager.getAllPlayerData()) {
            if (playerData.getIps().contains(joiningPlayerIp)) {
                altAccountNames.add(playerData.getName());
            }
        }

        altAccountNames.add(joiningPlayer.getName());

        if (altAccountNames.size() > 1) {
            plugin.getLogger().info("IP Address for " + joiningPlayer.getName() + " is " + joiningPlayerIp);
            plugin.getLogger().info("Found " + altAccountNames.size() + " total accounts on this IP: " + String.join(", ", altAccountNames));

            String otherAccounts = altAccountNames.stream()
                    .filter(name -> !name.equalsIgnoreCase(joiningPlayer.getName()))
                    .collect(Collectors.joining(", "));

            if (otherAccounts.isEmpty()) {
                return;
            }

            PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                    .setPlayerName(joiningPlayer.getName())
                    .setAltPlayer(otherAccounts);

            String alertMessage = configManager.getMessage("alt-account-alert");
            String formattedMessage = PlaceholderAPI.setPlaceholders(alertMessage, data);

            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("jsecurity.alt.alert"))
                    .forEach(p -> p.sendMessage(formattedMessage));
        }
    }
}