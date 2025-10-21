package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.StaffChatManager;
import com.jerae.jsecurity.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
                    onlinePlayer.sendMessage(format);
                }
            }
            event.setCancelled(true);
            return;
        }

        if (configManager.isChatDelayEnabled() && !player.hasPermission("jsecurity.chat.delay.bypass")) {
            if (chatDelay.containsKey(player.getUniqueId())) {
                long timeLeft = (long) ((chatDelay.get(player.getUniqueId()) + (configManager.getChatDelay() * 1000L)) - System.currentTimeMillis());
                if (timeLeft > 0) {
                    player.sendMessage(configManager.getChatDelayMessage().replace("{time}", String.format("%.2f", timeLeft / 1000.0)));
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

            List<String> sortedKeywords = replacementMap.keySet().stream()
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .collect(Collectors.toList());

            for (String keyword : sortedKeywords) {
                if (message.toLowerCase().contains(keyword.toLowerCase())) {
                    String replacement = replacementMap.get(keyword);
                    String literalKeyword = Pattern.quote(keyword);
                    String literalReplacement = Matcher.quoteReplacement(replacement);
                    message = message.replaceAll("(?i)" + literalKeyword, literalReplacement);
                }
            }
            event.setMessage(message);
        }
    }
}