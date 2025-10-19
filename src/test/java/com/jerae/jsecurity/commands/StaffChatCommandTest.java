package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.StaffChatManager;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;

class StaffChatCommandTest {

    @Mock
    private StaffChatManager staffChatManager;

    @Mock
    private ConfigManager configManager;

    @Mock
    private Player player;

    @Mock
    private Command command;

    @InjectMocks
    private StaffChatCommand staffChatCommand;

    private MockedStatic<Bukkit> bukkitMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bukkitMock = mockStatic(Bukkit.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(configManager.getStaffChatToggleMessage(anyBoolean())).thenReturn("some message");
    }

    @AfterEach
    void tearDown() {
        bukkitMock.close();
    }

    @Test
    void onCommand_ToggleStaffChatOn() {
        // Given
        when(player.hasPermission("jsecurity.staffchat")).thenReturn(true);
        when(staffChatManager.isInStaffChat(player.getUniqueId())).thenReturn(true);
        when(configManager.getStaffChatToggleMessage(true)).thenReturn("Staff chat toggled on.");

        // When
        staffChatCommand.onCommand(player, command, "sc", new String[]{"toggle"});

        // Then
        verify(staffChatManager).toggleStaffChat(player.getUniqueId());
        verify(player).sendMessage("Staff chat toggled on.");
    }

    @Test
    void onCommand_ToggleStaffChatOff() {
        // Given
        when(player.hasPermission("jsecurity.staffchat")).thenReturn(true);
        when(staffChatManager.isInStaffChat(player.getUniqueId())).thenReturn(false);
        when(configManager.getStaffChatToggleMessage(false)).thenReturn("Staff chat toggled off.");

        // When
        staffChatCommand.onCommand(player, command, "sc", new String[]{"toggle"});

        // Then
        verify(staffChatManager).toggleStaffChat(player.getUniqueId());
        verify(player).sendMessage("Staff chat toggled off.");
    }

    @Test
    void onCommand_SendMessageToStaffChat() {
        // Given
        when(player.hasPermission("jsecurity.staffchat")).thenReturn(true);
        when(configManager.getStaffChatMessageFormat()).thenReturn("&8[&cStaffChat&8] &7{player}: {message}");
        when(player.getName()).thenReturn("TestPlayer");
        Player staffPlayer = mock(Player.class);
        when(staffPlayer.hasPermission("jsecurity.staffchat")).thenReturn(true);
        bukkitMock.when(Bukkit::getOnlinePlayers).thenReturn(Collections.singletonList(staffPlayer));


        // When
        staffChatCommand.onCommand(player, command, "sc", new String[]{"hello", "staff"});

        // Then
        verify(staffPlayer).sendMessage("&8[&cStaffChat&8] &7TestPlayer: {message}");
    }

    @Test
    void onCommand_NoPermission() {
        // Given
        when(player.hasPermission("jsecurity.staffchat")).thenReturn(false);
        when(configManager.getNoPermissionMessage()).thenReturn("No permission");

        // When
        staffChatCommand.onCommand(player, command, "sc", new String[]{"test"});

        // Then
        verify(player).sendMessage("No permission");
    }
}