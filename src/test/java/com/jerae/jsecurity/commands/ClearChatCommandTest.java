package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import org.bukkit.Bukkit;
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

import static org.mockito.Mockito.*;

class ClearChatCommandTest {

    @Mock
    private ConfigManager configManager;

    @Mock
    private Player player;

    @Mock
    private Command command;

    @InjectMocks
    private ClearChatCommand clearChatCommand;

    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getOnlinePlayers).thenReturn(Collections.singletonList(player));
        when(configManager.getNoPermissionMessage()).thenReturn("No permission");
        when(configManager.getChatClearedMessage()).thenReturn("Chat cleared");
    }

    @AfterEach
    void tearDown() {
        mockedBukkit.close();
    }

    @Test
    void onCommand_noPermission() {
        when(player.hasPermission("jsecurity.clearchat")).thenReturn(false);
        clearChatCommand.onCommand(player, command, "clearchat", new String[]{});
        verify(player).sendMessage("No permission");
    }

    @Test
    void onCommand_withPermission() {
        when(player.hasPermission("jsecurity.clearchat")).thenReturn(true);
        clearChatCommand.onCommand(player, command, "clearchat", new String[]{});
        verify(player, times(100)).sendMessage("");
        verify(player).sendMessage("Chat cleared");
    }
}
