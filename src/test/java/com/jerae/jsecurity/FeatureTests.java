package com.jerae.jsecurity;

import com.jerae.jsecurity.commands.*;
import com.jerae.jsecurity.listeners.PlayerDataListener;
import com.jerae.jsecurity.listeners.PlayerListener;
import com.jerae.jsecurity.managers.*;
import com.jerae.jsecurity.models.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import com.jerae.jsecurity.managers.AuthManager;

public class FeatureTests {

    private MockedStatic<Bukkit> bukkit;
    private Server server;
    private Player player;
    private CommandSender sender;
    private final UUID playerUUID = UUID.randomUUID();
    private OfflinePlayer offlinePlayer;
    private PunishmentManager punishmentManager;
    private ConfigManager configManager;


    @BeforeEach
    public void setUp() {
        server = mock(Server.class);
        player = mock(Player.class);
        sender = mock(CommandSender.class);
        offlinePlayer = mock(OfflinePlayer.class);
        punishmentManager = mock(PunishmentManager.class);
        configManager = mock(ConfigManager.class);

        when(player.getName()).thenReturn("testPlayer");
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(sender.getName()).thenReturn("sender");

        when(offlinePlayer.getName()).thenReturn("testPlayer");
        when(offlinePlayer.getUniqueId()).thenReturn(playerUUID);
        when(offlinePlayer.hasPlayedBefore()).thenReturn(true);

        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getServer).thenReturn(server);
        bukkit.when(() -> Bukkit.getPlayer(eq("testPlayer"))).thenReturn(player);
        when(sender.hasPermission(anyString())).thenReturn(true);
        bukkit.when(() -> Bukkit.getOfflinePlayer(anyString())).thenReturn(offlinePlayer);
        ConsoleCommandSender console = mock(ConsoleCommandSender.class);
        bukkit.when(Bukkit::getConsoleSender).thenReturn(console);
    }

    @AfterEach
    public void tearDown() {
        bukkit.close();
    }

    @Test
    public void testKickCommand() {
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getMessage(anyString(), anyBoolean())).thenReturn("Kicked for a reason.");
        when(configManager.getMessage(anyString())).thenReturn("Kicked for a reason.");
        KickCommand kickCommand = new KickCommand(configManager);
        kickCommand.onCommand(sender, mock(Command.class), "kick", new String[]{"testPlayer", "test reason"});
        verify(player).kick(any(Component.class));
        verify(server).broadcast(any(Component.class));
    }

    @Test
    public void testKickCommandSilent() {
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getMessage(anyString(), anyBoolean())).thenReturn("Kicked silently.");
        KickCommand kickCommand = new KickCommand(configManager);
        kickCommand.onCommand(sender, mock(Command.class), "kick", new String[]{"testPlayer", "test reason", "-s"});
        verify(player).kick(any(Component.class));
        verify(server, never()).broadcast(any(Component.class));
    }

    @Test
    public void testUnbanCommandSilent() {
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        BanEntry banEntry = new BanEntry(playerUUID, "testPlayer", null, "reason", "staff", -1);
        when(punishmentManager.getBan(any(UUID.class))).thenReturn(banEntry);
        when(punishmentManager.isBanned(any(UUID.class))).thenReturn(true);
        ConfigManager configManager = mock(ConfigManager.class);
        UnbanCommand unbanCommand = new UnbanCommand(punishmentManager, configManager);
        unbanCommand.onCommand(sender, mock(Command.class), "unban", new String[]{"testPlayer", "-s"});
        verify(punishmentManager).removeBan(any());
        verify(server, never()).broadcast(any(Component.class));
    }

    @Test
    public void testUnmuteCommandSilent() {
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        MuteEntry muteEntry = new MuteEntry(playerUUID, "testPlayer", "reason", "staff", -1);
        when(punishmentManager.getMute(any(UUID.class))).thenReturn(muteEntry);
        when(punishmentManager.isMuted(any(UUID.class))).thenReturn(true);
        ConfigManager configManager = mock(ConfigManager.class);
        UnmuteCommand unmuteCommand = new UnmuteCommand(punishmentManager, configManager);
        unmuteCommand.onCommand(sender, mock(Command.class), "unmute", new String[]{"testPlayer", "-s"});
        verify(punishmentManager).removeMute(any());
        verify(server, never()).broadcast(any(Component.class));
    }

    @Test
    public void testReloadCommand() {
        JSecurity plugin = mock(JSecurity.class);
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getReloadMessage()).thenReturn("Configuration reloaded.");
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        PlayerDataManager playerDataManager = mock(PlayerDataManager.class);
        JSecurityCommand jSecurityCommand = new JSecurityCommand(plugin, configManager, punishmentManager, playerDataManager);
        jSecurityCommand.onCommand(sender, mock(Command.class), "js", new String[]{"reload"});
        verify(configManager).reloadConfig();
        verify(sender).sendMessage(anyString());
    }

    @Test
    public void testFreezeCommand() {
        FreezeManager freezeManager = new FreezeManager();
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getMessage(anyString())).thenReturn("You are frozen.");
        FreezeCommand freezeCommand = new FreezeCommand(freezeManager, configManager);
        freezeCommand.onCommand(sender, mock(Command.class), "freeze", new String[]{"testPlayer"});
        assertTrue(freezeManager.isFrozen(player));
        verify(sender).sendMessage(anyString());
    }

    @Test
    public void testUnfreezeCommand() {
        FreezeManager freezeManager = new FreezeManager();
        freezeManager.freeze(player);
        assertTrue(freezeManager.isFrozen(player));

        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getMessage("unfreeze-message")).thenReturn("You are unfrozen.");
        UnfreezeCommand unfreezeCommand = new UnfreezeCommand(freezeManager, configManager);
        unfreezeCommand.onCommand(sender, mock(Command.class), "unfreeze", new String[]{"testPlayer"});
        assertFalse(freezeManager.isFrozen(player));
        verify(sender).sendMessage(anyString());
    }

    @Test
    public void testJsRecordCommand() {
        JSecurity plugin = mock(JSecurity.class);
        ConfigManager configManager = mock(ConfigManager.class);
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        PlayerDataManager playerDataManager = mock(PlayerDataManager.class);

        List<PlayerData> players = new ArrayList<>();
        players.add(new PlayerData(1, UUID.randomUUID(), "PlayerA", Collections.singletonList("1.1.1.1"), "2025-01-01, 12:00"));
        players.add(new PlayerData(2, UUID.randomUUID(), "PlayerB", Collections.singletonList("2.2.2.2"), "2025-01-02, 12:00"));

        when(playerDataManager.getAllPlayerData()).thenReturn(players);

        JSecurityCommand jSecurityCommand = new JSecurityCommand(plugin, configManager, punishmentManager, playerDataManager);
        jSecurityCommand.onCommand(sender, mock(Command.class), "js", new String[]{"record"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender, times(3)).sendMessage(captor.capture()); // Title + 2 players

        List<String> messages = captor.getAllValues();
        assertEquals(ChatColor.GOLD + "--- Player Records (Page 1/1) ---", messages.get(0));
        assertEquals(ChatColor.YELLOW + "" + 1 + ". " + ChatColor.WHITE + "PlayerA", messages.get(1));
        assertEquals(ChatColor.YELLOW + "" + 2 + ". " + ChatColor.WHITE + "PlayerB", messages.get(2));
    }

    @Test
    public void testPlayerDataListenerNewPlayer() throws Exception {
        PlayerDataManager playerDataManager = mock(PlayerDataManager.class);
        ConfigManager configManager = mock(ConfigManager.class);
        PlayerJoinEvent event = mock(PlayerJoinEvent.class);
        Player newPlayer = mock(Player.class);
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName("1.2.3.4"), 12345);

        when(newPlayer.getUniqueId()).thenReturn(playerUUID);
        when(newPlayer.getName()).thenReturn("testPlayer");
        when(newPlayer.getAddress()).thenReturn(address);
        when(event.getPlayer()).thenReturn(newPlayer);

        when(playerDataManager.getPlayerData(playerUUID)).thenReturn(null);
        when(configManager.isAnnounceNewPlayerEnabled()).thenReturn(true);
        when(configManager.getAnnounceMilestones()).thenReturn(Collections.emptyList());
        when(playerDataManager.getAllPlayerData()).thenReturn(Collections.singletonList(mock(PlayerData.class)));
        when(configManager.getNewPlayerBroadcastMessage()).thenReturn("Welcome our {player_count}th player!");

        PlayerDataListener listener = new PlayerDataListener(playerDataManager, configManager);
        listener.onPlayerJoin(event);

        verify(playerDataManager).createPlayerData(playerUUID, "testPlayer", "1.2.3.4");
        verify(server).broadcast(any(Component.class));
    }

    @Test
    public void testAltAccountAlert() throws Exception {
        // Given
        JSecurity plugin = mock(JSecurity.class);
        java.util.logging.Logger logger = mock(java.util.logging.Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        ConfigManager configManager = mock(ConfigManager.class);
        PlayerDataManager playerDataManager = mock(PlayerDataManager.class);
        AuthManager authManager = mock(AuthManager.class);

        PlayerListener listener = new PlayerListener(plugin, punishmentManager, configManager, playerDataManager, authManager);

        Player player1 = mock(Player.class);
        when(player1.getName()).thenReturn("Player1");
        when(player1.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player1.hasPermission("jsecurity.alt.alert")).thenReturn(true);

        Player player2 = mock(Player.class);
        when(player2.getName()).thenReturn("Player2");
        when(player2.getUniqueId()).thenReturn(UUID.randomUUID());

        InetAddress ipAddress = InetAddress.getByName("127.0.0.1");
        InetSocketAddress socketAddress = new InetSocketAddress(ipAddress, 12345);
        when(player2.getAddress()).thenReturn(socketAddress);

        List<PlayerData> allPlayerData = new ArrayList<>();
        allPlayerData.add(new PlayerData(1, player1.getUniqueId(), "Player1", Collections.singletonList("127.0.0.1"), ""));

        when(playerDataManager.getAllPlayerData()).thenReturn(allPlayerData);
        when(configManager.isAltAccountAlertEnabled()).thenReturn(true);
        when(configManager.getMessage("alt-account-alert")).thenReturn("&c{player} may be an alt of {alt_player}.");
        bukkit.when(Bukkit::getOnlinePlayers).thenReturn(Collections.singletonList(player1));

        // When
        PlayerJoinEvent joinEvent = mock(PlayerJoinEvent.class);
        when(joinEvent.getPlayer()).thenReturn(player2);
        listener.onPlayerJoin(joinEvent);

        // Then
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(player1).sendMessage(captor.capture());

        Component expectedMessage = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize("&cPlayer2 may be an alt of Player1.");
        assertEquals(expectedMessage, captor.getValue());

        // Verify console output
        verify(logger).info("IP Address for Player2 is 127.0.0.1");
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(2)).info(logCaptor.capture());
        assertTrue(logCaptor.getAllValues().get(1).contains("Player1"));
        assertTrue(logCaptor.getAllValues().get(1).contains("Player2"));
    }

    @Test
    public void testMutedPlayerCommandRestriction() {
        // Given
        PlayerListener playerListener = new PlayerListener(mock(JSecurity.class), punishmentManager, configManager, mock(PlayerDataManager.class), mock(AuthManager.class));
        MuteEntry muteEntry = new MuteEntry(playerUUID, player.getName(), "test reason", "test staff", -1);

        when(punishmentManager.isMuted(playerUUID)).thenReturn(true);
        when(punishmentManager.getMute(playerUUID)).thenReturn(muteEntry);
        when(configManager.getMutedCommandRestriction()).thenReturn(List.of("msg", "tell"));
        String noPermissionMessage = "&cYou do not have permission to use this command.";
        when(configManager.getNoPermissionMessage()).thenReturn(noPermissionMessage);

        // When
        org.bukkit.event.player.PlayerCommandPreprocessEvent event = new org.bukkit.event.player.PlayerCommandPreprocessEvent(player, "/msg hello");
        playerListener.onPlayerCommandPreprocess(event);

        // Then
        assertTrue(event.isCancelled());
    }

    @Test
    public void testLoginCommandIsCancelled() {
        // Given
        AuthManager authManager = mock(AuthManager.class);
        PlayerListener playerListener = new PlayerListener(mock(JSecurity.class), punishmentManager, configManager, mock(PlayerDataManager.class), authManager);
        when(configManager.getBoolean("authentication.enabled")).thenReturn(true);
        when(authManager.isLoggedIn(player)).thenReturn(false);

        // When
        org.bukkit.event.player.PlayerCommandPreprocessEvent event = new org.bukkit.event.player.PlayerCommandPreprocessEvent(player, "/login password");
        playerListener.onPlayerCommandPreprocess(event);

        // Then
        assertTrue(event.isCancelled());
    }

    @Test
    public void testPlayerLogin() {
        // Given
        AuthManager authManager = mock(AuthManager.class);
        LoginCommand loginCommand = new LoginCommand(authManager, configManager);
        when(configManager.getBoolean("authentication.enabled")).thenReturn(true);
        when(authManager.isRegistered(playerUUID)).thenReturn(true);
        when(authManager.checkPassword(playerUUID, "password")).thenReturn(true);

        // When
        loginCommand.onCommand(player, mock(Command.class), "login", new String[]{"password"});

        // Then
        verify(authManager).loginPlayer(player);
    }

    @Test
    public void testPlayerRegister() {
        // Given
        AuthManager authManager = mock(AuthManager.class);
        RegisterCommand registerCommand = new RegisterCommand(authManager, configManager);
        when(configManager.getBoolean("authentication.enabled")).thenReturn(true);
        when(authManager.isRegistered(playerUUID)).thenReturn(false);

        // When
        registerCommand.onCommand(player, mock(Command.class), "register", new String[]{"password", "password"});

        // Then
        verify(authManager).registerPlayer(playerUUID, "password");
    }

    @Test
    public void testUnauthenticatedPlayerCannotMove() {
        // Given
        AuthManager authManager = mock(AuthManager.class);
        PlayerListener playerListener = new PlayerListener(mock(JSecurity.class), punishmentManager, configManager, mock(PlayerDataManager.class), authManager);
        when(configManager.getBoolean("authentication.enabled")).thenReturn(true);
        when(authManager.isLoggedIn(player)).thenReturn(false);

        // When
        org.bukkit.event.player.PlayerMoveEvent event = new org.bukkit.event.player.PlayerMoveEvent(player, player.getLocation(), player.getLocation().add(1, 0, 0));
        playerListener.onPlayerMove(event);

        // Then
        assertTrue(event.isCancelled());
    }
}