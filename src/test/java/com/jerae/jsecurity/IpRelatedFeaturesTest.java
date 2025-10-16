package com.jerae.jsecurity;

import com.jerae.jsecurity.managers.*;
import com.jerae.jsecurity.models.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IpRelatedFeaturesTest {

    @Mock
    private JSecurity plugin;
    @Mock
    private PlayerDataManager playerDataManager;
    @Mock
    private PunishmentManager punishmentManager;
    @Mock
    private ConfigManager configManager;
    @Mock
    private Player player;
    @Mock
    private InetSocketAddress address;
    @Mock
    private InetAddress inetAddress;
    @Mock
    private Logger logger;
    @Mock
    private Server server;

    private IpManager ipManager;

    private final UUID playerUUID = UUID.randomUUID();
    private final String playerName = "testPlayer";
    private final String playerIp = "127.0.0.1";
    private final UUID bannedUUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ipManager = new IpManager(plugin, playerDataManager, punishmentManager, configManager);
        lenient().when(player.getUniqueId()).thenReturn(playerUUID);
        lenient().when(player.getName()).thenReturn(playerName);
        lenient().when(player.getAddress()).thenReturn(address);
        lenient().when(address.getAddress()).thenReturn(inetAddress);
        lenient().when(inetAddress.getHostAddress()).thenReturn(playerIp);
        lenient().when(plugin.getLogger()).thenReturn(logger);
    }

    @Test
    void testBanEvasionIsTriggered() {
        try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
            bukkitMock.when(Bukkit::getServer).thenReturn(server);
            // Given
            when(configManager.isBanEvasionPreventionEnabled()).thenReturn(true);
            BanEntry ipBan = new BanEntry(bannedUUID, "bannedPlayer", "bannedIp", "reason", "staff", -1L);
            when(punishmentManager.getBanByIp(playerIp)).thenReturn(ipBan);
            when(configManager.getMessage(eq("ban-evasion-kick-message"))).thenReturn("evasion kick");
            when(configManager.getMessage(eq("ban-evasion-reason"))).thenReturn("evasion reason");

            // When
            ipManager.handlePlayerIp(player);

            // Then
            verify(punishmentManager).addBan(any(BanEntry.class));
            verify(player).kick(any(Component.class));
        }
    }

    @Test
    void testAltAccountAlertIsNotTriggeredForSingleAccount() {
        try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
            bukkitMock.when(Bukkit::getServer).thenReturn(server);
            // Given
            when(configManager.isAltAccountAlertEnabled()).thenReturn(true);
            when(playerDataManager.getAllPlayerData()).thenReturn(Collections.emptyList());

            // When
            ipManager.handlePlayerIp(player);

            // Then
            verify(logger, never()).info(contains("Found"));
        }
    }

    @Test
    void testIpIsLoggedForNewPlayer() {
        // Given
        when(playerDataManager.getPlayerData(playerUUID)).thenReturn(null);

        // When
        ipManager.handlePlayerIp(player);

        // Then
        verify(playerDataManager).createPlayerData(playerUUID, playerName, playerIp);
    }

    @Test
    void testIpIsUpdatedForExistingPlayer() {
        // Given
        PlayerData playerData = new PlayerData(1, playerUUID, playerName, new java.util.ArrayList<>(), "joined");
        when(playerDataManager.getPlayerData(playerUUID)).thenReturn(playerData);

        // When
        ipManager.handlePlayerIp(player);

        // Then
        verify(playerDataManager).updatePlayerData(playerData);
        assertTrue(playerData.getIps().contains(playerIp));
    }

    @Test
    void testAltAccountAlertIsTriggered() {
        try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
            bukkitMock.when(Bukkit::getServer).thenReturn(server);
            // Given
            when(configManager.isAltAccountAlertEnabled()).thenReturn(true);
            PlayerData altPlayerData = mock(PlayerData.class);
            when(altPlayerData.getIps()).thenReturn(Collections.singletonList(playerIp));
            when(altPlayerData.getName()).thenReturn("altPlayer");
            when(playerDataManager.getAllPlayerData()).thenReturn(Collections.singletonList(altPlayerData));
            when(configManager.getMessage("alt-account-alert")).thenReturn("Alert for {player}");
            Player onlinePlayer = mock(Player.class);
            when(onlinePlayer.hasPermission("jsecurity.alt.alert")).thenReturn(true);
            when(server.getOnlinePlayers()).thenReturn(Collections.singletonList(onlinePlayer));

            // When
            ipManager.handlePlayerIp(player);

            // Then
            verify(logger).info(contains("Found 2 total accounts"));
            verify(onlinePlayer).sendMessage(any(Component.class));
        }
    }
}