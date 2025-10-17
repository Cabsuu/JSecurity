package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.StaffChatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatListenerTest {

    @Mock
    private ConfigManager configManager;

    @Mock
    private StaffChatManager staffChatManager;

    @Mock
    private Player player;

    @InjectMocks
    private ChatListener chatListener;

    @BeforeEach
    void setUp() {
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(staffChatManager.isInStaffChat(any(UUID.class))).thenReturn(false);
    }

    @Test
    void onPlayerChat_KeywordReplacementIsDeterministic() {
        // Given
        when(configManager.isKeywordReplacementEnabled()).thenReturn(true);

        // This map has a replacement chain: "bad" -> "good", "good" -> "excellent"
        // The current implementation is non-deterministic. If "good" is processed first,
        // "this is good" becomes "this is excellent". If "bad" is processed first,
        // "this is bad" becomes "this is good", which could then become "this is excellent"
        // in the same pass, which is not the desired, predictable behavior.
        // A deterministic approach would replace "bad" with "good" and stop, not re-process the output.
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("bad", "good");
        replacementMap.put("good", "excellent");
        when(configManager.getKeywordReplacementMap()).thenReturn(replacementMap);

        // Mock the event for the message "this is bad"
        AsyncPlayerChatEvent badMessageEvent = new AsyncPlayerChatEvent(false, player, "this is bad", null);

        // Mock the event for the message "this is good"
        AsyncPlayerChatEvent goodMessageEvent = new AsyncPlayerChatEvent(false, player, "this is good", null);

        // When
        chatListener.onPlayerChat(badMessageEvent);
        chatListener.onPlayerChat(goodMessageEvent);

        // Then
        // The replacement should be predictable. "bad" becomes "good", and "good" becomes "excellent".
        // The result should not depend on map iteration order. One pass only.
        assertEquals("this is good", badMessageEvent.getMessage(), "The word 'bad' should be replaced by 'good' and not be re-processed.");
        assertEquals("this is excellent", goodMessageEvent.getMessage(), "The word 'good' should be replaced by 'excellent'.");
    }

    @Test
    void onPlayerChat_KeywordReplacementBypass() {
        // Given
        when(configManager.isKeywordReplacementEnabled()).thenReturn(true);
        when(player.hasPermission("jsecurity.replaceword.bypass")).thenReturn(true);
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("bad", "good");
        when(configManager.getKeywordReplacementMap()).thenReturn(replacementMap);

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, player, "this is bad", null);

        // When
        chatListener.onPlayerChat(event);

        // Then
        assertEquals("this is bad", event.getMessage(), "Player with bypass permission should not have their message modified.");
    }
}