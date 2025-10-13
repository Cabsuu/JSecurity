package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.managers.ConfigManager;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.bukkit.entity.Player;

public class ChatListenerTest {

    private ConfigManager configManager;
    private ChatListener chatListener;
    private Player player;

    @BeforeEach
    public void setUp() {
        configManager = Mockito.mock(ConfigManager.class);
        chatListener = new ChatListener(configManager);
        player = Mockito.mock(Player.class);
    }

    @Test
    public void testKeywordReplacementWholeWord() {
        // Given
        when(configManager.isKeywordReplacementEnabled()).thenReturn(true);
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("fuck", "fudge");
        replacementMap.put("fck", "fudge");
        replacementMap.put("fk", "fudge");
        replacementMap.put("shit", "poop");
        when(configManager.getKeywordReplacementMap()).thenReturn(replacementMap);

        // When
        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, player, "you are a fcking idiot", null);
        chatListener.onPlayerChat(event);

        // Then
        assertEquals("you are a fcking idiot", event.getMessage());

        // When
        event = new AsyncPlayerChatEvent(false, player, "fuck you", null);
        chatListener.onPlayerChat(event);

        // Then
        assertEquals("fudge you", event.getMessage());

        // When
        event = new AsyncPlayerChatEvent(false, player, "what the fuck!", null);
        chatListener.onPlayerChat(event);

        // Then
        assertEquals("what the fudge!", event.getMessage());

        // When
        event = new AsyncPlayerChatEvent(false, player, "this is a test, not a fckn test", null);
        chatListener.onPlayerChat(event);

        // Then
        assertEquals("this is a test, not a fckn test", event.getMessage());

        // When
        event = new AsyncPlayerChatEvent(false, player, "fk", null);
        chatListener.onPlayerChat(event);

        // Then
        assertEquals("fudge", event.getMessage());

        // When
        event = new AsyncPlayerChatEvent(false, player, "that's some real shit", null);
        chatListener.onPlayerChat(event);

        // Then
        assertEquals("that's some real poop", event.getMessage());
    }
}