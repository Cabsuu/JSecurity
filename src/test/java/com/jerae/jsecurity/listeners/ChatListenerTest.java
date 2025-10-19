package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.StaffChatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ChatListenerTest {

    @Mock
    private ConfigManager configManager;

    @Mock
    private StaffChatManager staffChatManager;

    @Mock
    private Player player;

    @Mock
    private AsyncPlayerChatEvent event;

    private ChatListener chatListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatListener = new ChatListener(configManager, staffChatManager);
    }

    @Test
    void testKeywordReplacement() {
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("badword", "goodword");
        replacementMap.put("anotherbadword", "anothergoodword");

        when(configManager.isKeywordReplacementEnabled()).thenReturn(true);
        when(player.hasPermission("jsecurity.replaceword.bypass")).thenReturn(false);
        when(configManager.getKeywordReplacementMap()).thenReturn(replacementMap);
        when(event.getPlayer()).thenReturn(player);
        doCallRealMethod().when(event).getMessage();
        doCallRealMethod().when(event).setMessage(anyString());
        event.setMessage("This is a badword and anotherbadword");

        chatListener.onPlayerChat(event);

        assertEquals("This is a goodword and anothergoodword", event.getMessage());
    }

    @Test
    void testKeywordReplacementWithPartialWord() {
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("bad", "good");

        when(configManager.isKeywordReplacementEnabled()).thenReturn(true);
        when(player.hasPermission("jsecurity.replaceword.bypass")).thenReturn(false);
        when(configManager.getKeywordReplacementMap()).thenReturn(replacementMap);
        when(event.getPlayer()).thenReturn(player);
        when(event.getMessage()).thenReturn("This is a badword");

        chatListener.onPlayerChat(event);

        assertEquals("This is a badword", event.getMessage());
    }
}
