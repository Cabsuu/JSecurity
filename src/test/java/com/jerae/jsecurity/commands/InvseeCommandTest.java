package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.InvseeManager;
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

import static org.mockito.Mockito.*;

class InvseeCommandTest {

    @Mock
    private InvseeManager invseeManager;

    @Mock
    private ConfigManager configManager;

    @Mock
    private Command command;

    @InjectMocks
    private InvseeCommand invseeCommand;
    private AutoCloseable closeable;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        mockedBukkit = mockStatic(Bukkit.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    @Test
    void onCommand_shouldOpenInventory_whenPlayerHasPermission() {
        // Given
        Player staff = mock(Player.class);
        when(staff.hasPermission("jsecurity.invsee")).thenReturn(true);
        Player target = mock(Player.class);
        when(target.getName()).thenReturn("target");
        mockedBukkit.when(() -> Bukkit.getPlayer("target")).thenReturn(target);

        // When
        invseeCommand.onCommand(staff, command, "invsee", new String[]{"target"});

        // Then
        verify(invseeManager).openInventory(staff, target);
    }

    @Test
    void onCommand_shouldSendMessage_whenPlayerDoesNotHavePermission() {
        // Given
        Player staff = mock(Player.class);
        when(staff.hasPermission("jsecurity.invsee")).thenReturn(false);
        when(configManager.getNoPermissionMessage()).thenReturn("No permission.");

        // When
        invseeCommand.onCommand(staff, command, "invsee", new String[]{"target"});

        // Then
        verify(staff).sendMessage(anyString());
        verify(invseeManager, never()).openInventory(any(), any());
    }

    @Test
    void onCommand_shouldSendMessage_whenTargetPlayerNotFound() {
        // Given
        Player staff = mock(Player.class);
        when(staff.hasPermission("jsecurity.invsee")).thenReturn(true);
        mockedBukkit.when(() -> Bukkit.getPlayer("target")).thenReturn(null);
        when(configManager.getPlayerNotFoundMessage()).thenReturn("Player not found.");

        // When
        invseeCommand.onCommand(staff, command, "invsee", new String[]{"target"});

        // Then
        verify(staff).sendMessage(anyString());
        verify(invseeManager, never()).openInventory(any(), any());
    }

    @Test
    void onCommand_shouldSendMessage_whenNoTargetPlayerSpecified() {
        // Given
        Player staff = mock(Player.class);
        when(staff.hasPermission("jsecurity.invsee")).thenReturn(true);
        when(configManager.getInvseeUsageMessage()).thenReturn("Usage message.");

        // When
        invseeCommand.onCommand(staff, command, "invsee", new String[]{});

        // Then
        verify(staff).sendMessage(anyString());
        verify(invseeManager, never()).openInventory(any(), any());
    }
}
