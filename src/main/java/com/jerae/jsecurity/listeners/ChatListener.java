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

            String[] words = message.split(" ");
            Pattern pattern = Pattern.compile("(\\p{Punct}*)(.*?)(\\p{Punct}*)");

            for (int i = 0; i < words.length; i++) {
                String originalWord = words[i];
                Matcher matcher = pattern.matcher(originalWord);

                if (matcher.matches()) {
                    String leadingPunct = matcher.group(1);
                    String coreWord = matcher.group(2);
                    String trailingPunct = matcher.group(3);

                    String wordToMatch = coreWord.toLowerCase();

                    if (replacementMap.containsKey(wordToMatch)) {
                        String replacement = replacementMap.get(wordToMatch);
                        words[i] = leadingPunct + replacement + trailingPunct;
                    }
                }
            }
            event.setMessage(String.join(" ", words));
        }
    }
}