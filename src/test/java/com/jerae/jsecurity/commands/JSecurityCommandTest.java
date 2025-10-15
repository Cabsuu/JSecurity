package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.managers.AuthManager;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PlayerDataManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JSecurityCommandTest {

    @Mock
    private JSecurity plugin;

    @Mock
    private ConfigManager configManager;

    @Mock
    private PunishmentManager punishmentManager;

    @Mock
    private PlayerDataManager playerDataManager;

    @Mock
    private AuthManager authManager;

    @Mock
    private Server server;

    @Mock
    private Player player;

    @Mock
    private Command command;

    @Mock
    private Logger logger;

    @InjectMocks
    private JSecurityCommand jSecurityCommand;

    private final UUID playerUUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(plugin.getLogger()).thenReturn(logger);
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.hasPermission(anyString())).thenReturn(true);
    }

    @Test
    void testUnregisterCommand() {
        try (MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {
            OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
            when(offlinePlayer.getUniqueId()).thenReturn(playerUUID);
            when(offlinePlayer.hasPlayedBefore()).thenReturn(true);
            bukkit.when(() -> Bukkit.getOfflinePlayer(anyString())).thenReturn(offlinePlayer);

            when(authManager.isRegistered(playerUUID)).thenReturn(true);

            jSecurityCommand.onCommand(player, command, "js", new String[]{"unregister", "testplayer"});
            jSecurityCommand.onCommand(player, command, "js", new String[]{"unregister", "testplayer"});

            verify(authManager).unregisterPlayer(playerUUID);
            verify(player).sendMessage(anyString());
        }
    }
}