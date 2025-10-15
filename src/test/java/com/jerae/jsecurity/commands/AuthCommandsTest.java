package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.managers.AuthManager;
import com.jerae.jsecurity.managers.ConfigManager;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthCommandsTest {

    @Mock
    private JSecurity plugin;

    @Mock
    private AuthManager authManager;

    @Mock
    private ConfigManager configManager;

    @Mock
    private Server server;

    @Mock
    private Player player;

    @Mock
    private Command command;

    @Mock
    private Logger logger;

    @InjectMocks
    private RegisterCommand registerCommand;

    @InjectMocks
    private LoginCommand loginCommand;

    @InjectMocks
    private UnregisterCommand unregisterCommand;

    @InjectMocks
    private ChangePassCommand changePassCommand;

    private final UUID playerUUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(plugin.getLogger()).thenReturn(logger);
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(configManager.getBoolean("authentication.enabled")).thenReturn(true);
    }

    @Test
    void testRegisterCommand() {
        when(authManager.isRegistered(playerUUID)).thenReturn(false);
        when(authManager.validatePassword(anyString())).thenReturn(null);

        registerCommand.onCommand(player, command, "register", new String[]{"password", "password"});

        verify(authManager).registerPlayer(playerUUID, "password");
        verify(player).sendMessage("You have been registered successfully. Please log in using /login <password>");
    }

    @Test
    void testRegisterCommandPasswordMismatch() {
        registerCommand.onCommand(player, command, "register", new String[]{"password", "wrongpassword"});

        verify(player).sendMessage("Passwords do not match.");
        verify(authManager, never()).registerPlayer(any(), any());
    }

    @Test
    void testRegisterCommandInvalidPassword() {
        when(authManager.validatePassword("weak")).thenReturn("Password is too short.");

        registerCommand.onCommand(player, command, "register", new String[]{"weak", "weak"});

        verify(player).sendMessage("Password is too short.");
        verify(authManager, never()).registerPlayer(any(), any());
    }

    @Test
    void testLoginCommand() {
        when(authManager.isRegistered(playerUUID)).thenReturn(true);
        when(authManager.checkPassword(playerUUID, "password")).thenReturn(true);

        loginCommand.onCommand(player, command, "login", new String[]{"password"});

        verify(authManager).loginPlayer(player);
        verify(player).sendMessage("You have logged in successfully.");
    }

    @Test
    void testLoginCommandIncorrectPassword() {
        when(authManager.isRegistered(playerUUID)).thenReturn(true);
        when(authManager.checkPassword(playerUUID, "wrongpassword")).thenReturn(false);

        loginCommand.onCommand(player, command, "login", new String[]{"wrongpassword"});

        verify(player).sendMessage("Incorrect password.");
        verify(authManager, never()).loginPlayer(any());
    }

    @Test
    void testUnregisterCommand() {
        when(authManager.isRegistered(playerUUID)).thenReturn(true);
        when(authManager.checkPassword(playerUUID, "password")).thenReturn(true);

        unregisterCommand.onCommand(player, command, "unregister", new String[]{"password"});
        unregisterCommand.onCommand(player, command, "unregister", new String[]{"password"});

        verify(authManager).unregisterPlayer(playerUUID);
        verify(player).sendMessage("You have been unregistered successfully.");
    }

    @Test
    void testChangePassCommand() {
        when(authManager.isRegistered(playerUUID)).thenReturn(true);
        when(authManager.checkPassword(playerUUID, "oldpassword")).thenReturn(true);
        when(authManager.validatePassword("newpassword")).thenReturn(null);

        changePassCommand.onCommand(player, command, "changepass", new String[]{"oldpassword", "newpassword"});

        verify(authManager).changePassword(playerUUID, "newpassword");
        verify(player).sendMessage("Your password has been changed successfully.");
    }
}