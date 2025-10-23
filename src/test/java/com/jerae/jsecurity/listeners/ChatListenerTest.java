package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.StaffChatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class ChatListenerTest {

    private ConfigManager configManager;
    private StaffChatManager staffChatManager;
    private ChatListener chatListener;
    private Player player;
    private AsyncPlayerChatEvent event;

    @BeforeEach
    public void setUp() {
        configManager = Mockito.mock(ConfigManager.class);
        staffChatManager = Mockito.mock(StaffChatManager.class);
        chatListener = new ChatListener(configManager, staffChatManager);
        player = Mockito.mock(Player.class);
        event = new AsyncPlayerChatEvent(false, player, "initial message", null);
    }

    @Test
    public void testKeywordReplacementLongestFirst() {
        when(configManager.isKeywordReplacementEnabled()).thenReturn(true);
        when(player.hasPermission("jsecurity.replaceword.bypass")).thenReturn(false);

        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("hell", "heck");
        replacementMap.put("hello", "hi");
        when(configManager.getKeywordReplacementMap()).thenReturn(replacementMap);

        event.setMessage("hello world");
        chatListener.onPlayerChat(event);

        assertEquals("hi world", event.getMessage());
    }

    @Test
    public void onPlayerChat_chatFilter_blocked() {
        when(configManager.isChatFilterEnabled()).thenReturn(true);
        when(player.hasPermission("jsecurity.chatfilter.bypass")).thenReturn(false);
        when(configManager.getBlockedKeywords()).thenReturn(java.util.Collections.singletonList("test"));
        event.setMessage("this is a test message");
        chatListener.onPlayerChat(event);
        assertEquals(true, event.isCancelled());
    }

    @Test
    public void onPlayerChat_chatFilter_notBlocked() {
        when(configManager.isChatFilterEnabled()).thenReturn(true);
        when(player.hasPermission("jsecurity.chatfilter.bypass")).thenReturn(false);
        when(configManager.getBlockedKeywords()).thenReturn(java.util.Collections.singletonList("test"));
        event.setMessage("this is a message");
        chatListener.onPlayerChat(event);
        assertEquals(false, event.isCancelled());
    }

    @Test
    public void onPlayerChat_chatFilter_bypass() {
        when(configManager.isChatFilterEnabled()).thenReturn(true);
        when(player.hasPermission("jsecurity.chatfilter.bypass")).thenReturn(true);
        when(configManager.getBlockedKeywords()).thenReturn(java.util.Collections.singletonList("test"));
        event.setMessage("this is a test message");
        chatListener.onPlayerChat(event);
        assertEquals(false, event.isCancelled());
    }
}
