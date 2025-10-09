package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PlayerDataManager;
import com.jerae.jsecurity.models.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.List;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerDataListener implements Listener {

    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;

    public PlayerDataListener(PlayerDataManager playerDataManager, ConfigManager configManager) {
        this.playerDataManager = playerDataManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ipAddress = player.getAddress().getAddress().getHostAddress();

        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        if (playerData == null) {
            // New player
            playerDataManager.createPlayerData(player.getUniqueId(), player.getName(), ipAddress);
            handleNewPlayerAnnouncement();
        } else {
            // Existing player
            playerData.addIp(ipAddress);
            playerDataManager.savePlayerData();
        }
    }

    private void handleNewPlayerAnnouncement() {
        if (!configManager.isAnnounceNewPlayerEnabled()) {
            return;
        }

        int playerCount = playerDataManager.getAllPlayerData().size();
        List<Integer> milestones = configManager.getAnnounceMilestones();

        if (milestones.isEmpty() || milestones.contains(playerCount)) {
            String message = configManager.getNewPlayerBroadcastMessage().replace("{player_count}", String.valueOf(playerCount));
            Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
            Bukkit.getServer().broadcast(component);
        }
    }
}