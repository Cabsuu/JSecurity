package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private final ConfigManager configManager;
    private final Map<UUID, Long> chatDelay = new HashMap<>();

    public ChatListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (configManager.isChatDelayEnabled() && !player.hasPermission("jsecurity.chat.delay.bypass")) {
            if (chatDelay.containsKey(player.getUniqueId())) {
                long timeLeft = (long) ((chatDelay.get(player.getUniqueId()) + (configManager.getChatDelay() * 1000L)) - System.currentTimeMillis());
                if (timeLeft > 0) {
                    player.sendMessage("You must wait " + String.format("%.2f", timeLeft / 1000.0) + " seconds before chatting again.");
                    event.setCancelled(true);
                    return;
                }
            }
            chatDelay.put(player.getUniqueId(), System.currentTimeMillis());
        }

        if (configManager.isKeywordReplacementEnabled()) {
            String message = event.getMessage();
            Map<String, String> replacementMap = configManager.getKeywordReplacementMap();

            StringBuilder newMessage = new StringBuilder();
            String[] words = message.split(" ");

            for (String word : words) {
                String coreWord = word.replaceAll("^\\p{Punct}+|\\p{Punct}+$", "");
                String lowerCaseCoreWord = coreWord.toLowerCase();

                String replacement = replacementMap.get(lowerCaseCoreWord);
                if (replacement != null) {
                    newMessage.append(word.replace(coreWord, replacement)).append(" ");
                } else {
                    newMessage.append(word).append(" ");
                }
            }
            event.setMessage(newMessage.toString().trim());
        }
    }
}