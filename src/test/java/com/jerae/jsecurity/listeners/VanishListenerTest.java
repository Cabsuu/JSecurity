package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.managers.VanishManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class VanishListenerTest {

    @Mock
    private JSecurity plugin;

    @Mock
    private VanishManager vanishManager;

    @Mock
    private Player player;

    @InjectMocks
    private VanishListener vanishListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void onPlayerQuit_VanishedPlayerWithSilentQuitPermission_QuitMessageIsNull() {
        // Given
        when(vanishManager.isVanished(player)).thenReturn(true);
        when(player.hasPermission("jsecurity.vanish.silentquit")).thenReturn(true);
        PlayerQuitEvent event = new PlayerQuitEvent(player, LegacyComponentSerializer.legacyAmpersand().deserialize("Player has left the game"));

        // When
        vanishListener.onPlayerQuit(event);

        // Then
        assert event.getQuitMessage() == null;
    }

    @Test
    void onPlayerQuit_VanishedPlayerWithoutSilentQuitPermission_QuitMessageIsNotNull() {
        // Given
        when(vanishManager.isVanished(player)).thenReturn(true);
        when(player.hasPermission("jsecurity.vanish.silentquit")).thenReturn(false);
        String message = "Player has left the game";
        PlayerQuitEvent event = new PlayerQuitEvent(player, message);

        // When
        vanishListener.onPlayerQuit(event);

        // Then
        assert event.getQuitMessage().equals(message);
    }
}