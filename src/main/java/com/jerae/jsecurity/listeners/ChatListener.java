package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.StaffChatManager;
import com.jerae.jsecurity.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatListener implements Listener {

    private final ConfigManager configManager;
    private final StaffChatManager staffChatManager;
    private final Map<UUID, Long> chatDelay = new HashMap<>();

    public ChatListener(ConfigManager configManager, StaffChatManager staffChatManager) {
        this.configManager = configManager;
        this.staffChatManager = staffChatManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (staffChatManager.isInStaffChat(player.getUniqueId())) {
            if (!player.hasPermission("jsecurity.staffchat")) {
                staffChatManager.toggleStaffChat(player.getUniqueId());
                return;
            }
            String message = event.getMessage();
            String format = configManager.getStaffChatMessageFormat()
                    .replace("{player}", player.getName())
                    .replace("{message}", message);

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("jsecurity.staffchat")) {
                    onlinePlayer.sendMessage(ColorUtil.colorize(format));
                }
            }
            event.setCancelled(true);
            return;
        }

        if (configManager.isChatDelayEnabled() && !player.hasPermission("jsecurity.chat.delay.bypass")) {
            if (chatDelay.containsKey(player.getUniqueId())) {
                long timeLeft = (long) ((chatDelay.get(player.getUniqueId()) + (configManager.getChatDelay() * 1000L)) - System.currentTimeMillis());
                if (timeLeft > 0) {
                    player.sendMessage(ColorUtil.colorize(configManager.getChatDelayMessage().replace("{time}", String.format("%.2f", timeLeft / 1000.0))));
                    event.setCancelled(true);
                    return;
                }
            }
            chatDelay.put(player.getUniqueId(), System.currentTimeMillis());
        }

        if (configManager.isKeywordReplacementEnabled() && !player.hasPermission("jsecurity.replaceword.bypass")) {
            String message = event.getMessage();
            Map<String, String> replacementMap = configManager.getKeywordReplacementMap();

            if (replacementMap == null || replacementMap.isEmpty()) {
                return;
            }

            for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
                String keyword = entry.getKey();
                String replacement = entry.getValue();

                Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(message);
                message = matcher.replaceAll(replacement);
            }
            event.setMessage(message);
        }
    }
}