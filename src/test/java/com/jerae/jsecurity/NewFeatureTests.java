package com.jerae.jsecurity;

import com.jerae.jsecurity.commands.JSecurityCommand;
import com.jerae.jsecurity.commands.WarnCommand;
import com.jerae.jsecurity.listeners.PlayerListener;
import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PlayerDataManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.models.PunishmentLogEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NewFeatureTests {

    private MockedStatic<Bukkit> bukkit;
    private Server server;
    private Player player;
    private Player altPlayer;
    private CommandSender sender;
    private JSecurity plugin;
    private final UUID playerUUID = UUID.randomUUID();
    private final UUID altPlayerUUID = UUID.randomUUID();
    private final String playerIp = "127.0.0.1";

    @BeforeEach
    public void setUp() throws Exception {
        server = mock(Server.class);
        player = mock(Player.class);
        altPlayer = mock(Player.class);
        sender = mock(CommandSender.class);
        plugin = mock(JSecurity.class);

        // Common player setup
        when(player.getName()).thenReturn("testPlayer");
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.getAddress()).thenReturn(new java.net.InetSocketAddress(InetAddress.getByName(playerIp), 12345));
        when(player.isOnline()).thenReturn(true);
        when(player.hasPlayedBefore()).thenReturn(true);

        // Alt player setup
        when(altPlayer.getName()).thenReturn("altPlayer");
        when(altPlayer.getUniqueId()).thenReturn(altPlayerUUID);
        when(altPlayer.getAddress()).thenReturn(new java.net.InetSocketAddress(InetAddress.getByName(playerIp), 54321));
        when(altPlayer.isOnline()).thenReturn(true);
        when(altPlayer.hasPlayedBefore()).thenReturn(true);

        // Plugin setup
        File testDataFolder = new File("target/test-data");
        testDataFolder.mkdirs();
        when(plugin.getDataFolder()).thenReturn(testDataFolder);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

        // Bukkit static mocks
        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getServer).thenReturn(server);
        bukkit.when(() -> Bukkit.getPlayer("testPlayer")).thenReturn(player);
        bukkit.when(() -> Bukkit.getOfflinePlayer("testPlayer")).thenReturn(player);
        bukkit.when(() -> Bukkit.getOfflinePlayer(playerUUID)).thenReturn(player);
        bukkit.when(() -> Bukkit.getOfflinePlayer("altPlayer")).thenReturn(altPlayer);
        bukkit.when(() -> Bukkit.getOfflinePlayer(altPlayerUUID)).thenReturn(altPlayer);

        doReturn(List.of(player, altPlayer)).when(server).getOnlinePlayers();
    }

    @AfterEach
    public void tearDown() {
        bukkit.close();
        File testDataFolder = new File("target/test-data");
        if (testDataFolder.exists()) {
            for (File file : testDataFolder.listFiles()) {
                file.delete();
            }
            testDataFolder.delete();
        }
    }

    @Test
    public void testBanEvasionKickMessage() throws Exception {
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        ConfigManager configManager = mock(ConfigManager.class);
        PlayerListener listener = new PlayerListener(punishmentManager, configManager);

        BanEntry ipBan = new BanEntry(UUID.randomUUID(), "bannedPlayer", playerIp, "reason", "staff", -1L);
        when(configManager.isBanEvasionPreventionEnabled()).thenReturn(true);
        when(punishmentManager.getBanByIp(playerIp)).thenReturn(ipBan);
        when(configManager.getMessage("kick-messages.ban-evasion")).thenReturn("Evading {banned_player}");
        when(configManager.getMessage("ban-evasion-reason")).thenReturn("Evasion of {banned_player}");

        AsyncPlayerPreLoginEvent event = mock(AsyncPlayerPreLoginEvent.class);
        when(event.getUniqueId()).thenReturn(playerUUID);
        when(event.getName()).thenReturn("testPlayer");
        when(event.getAddress()).thenReturn(InetAddress.getByName(playerIp));

        listener.onAsyncPlayerPreLogin(event);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(event).disallow(eq(AsyncPlayerPreLoginEvent.Result.KICK_BANNED), messageCaptor.capture());
        assertEquals("Evading bannedPlayer", messageCaptor.getValue());
        verify(punishmentManager).addBan(any(BanEntry.class));
    }

    @Test
    public void testWarnCommand() {
        PunishmentManager punishmentManager = new PunishmentManager(plugin);
        ConfigManager configManager = mock(ConfigManager.class);
        WarnCommand warnCommand = new WarnCommand(punishmentManager, configManager);

        when(configManager.getMessage("warn-message")).thenReturn("Warned: {reason}");
        when(configManager.getMessage("warn-broadcast")).thenReturn("{player} warned by {staff} for {reason}");

        warnCommand.onCommand(sender, mock(Command.class), "warn", new String[]{"testPlayer", "behaving", "badly"});

        assertEquals(1, punishmentManager.getPunishmentLogs().size());
        verify(player).sendMessage(eq("Warned: behaving badly"));
        verify(server).broadcast(any(Component.class));
    }

    @Test
    public void testAltAccountAlert() {
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        ConfigManager configManager = mock(ConfigManager.class);
        PlayerListener listener = new PlayerListener(punishmentManager, configManager);

        Player staff = mock(Player.class);
        when(staff.hasPermission("jsecurity.alt.alert")).thenReturn(true);
        doReturn(List.of(player, altPlayer, staff)).when(server).getOnlinePlayers();

        when(configManager.isAltAccountAlertEnabled()).thenReturn(true);
        when(configManager.getMessage("alt-account-alert")).thenReturn("Alt alert: {player} may be an alt of {alt_player}");

        PlayerJoinEvent event = new PlayerJoinEvent(altPlayer, LegacyComponentSerializer.legacyAmpersand().deserialize(""));
        listener.onPlayerJoin(event);

        verify(staff).sendMessage("Alt alert: altPlayer may be an alt of testPlayer");
    }

    @Test
    public void testAltAccountBanKick() {
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        ConfigManager configManager = mock(ConfigManager.class);
        PlayerListener listener = new PlayerListener(punishmentManager, configManager);

        when(configManager.isBanEvasionPreventionEnabled()).thenReturn(true);
        when(configManager.getMessage("kick-messages.alt-account-banned")).thenReturn("Alt account {banned_player} banned");

        listener.onPlayerBan(player, playerIp);

        verify(altPlayer).kick(any(Component.class));
    }

    @Test
    public void testPunishmentLogging() {
        PunishmentManager punishmentManager = new PunishmentManager(plugin);
        BanEntry ban = new BanEntry(playerUUID, "testPlayer", playerIp, "reason", "staff", -1L);
        punishmentManager.addBan(ban);

        List<PunishmentLogEntry> logs = punishmentManager.getPunishmentLogs();
        assertEquals(1, logs.size());
        assertEquals("Ban", logs.get(0).getPunishmentType());
    }

    @Test
    public void testJsLogCommand() {
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        JSecurityCommand command = new JSecurityCommand(plugin, mock(ConfigManager.class), punishmentManager, mock(PlayerDataManager.class));
        List<PunishmentLogEntry> logs = new ArrayList<>();
        logs.add(new PunishmentLogEntry("p1", UUID.randomUUID(), "Ban", "r1", "s1", 0));
        logs.add(new PunishmentLogEntry("p2", UUID.randomUUID(), "Mute", "r2", "s2", 0));
        when(punishmentManager.getPunishmentLogs()).thenReturn(logs);

        command.onCommand(sender, mock(Command.class), "js", new String[]{"log"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender, times(3)).sendMessage(captor.capture());

        List<String> messages = captor.getAllValues();
        assertEquals(ChatColor.GOLD + "--- Punishment Log (Page 1/1) ---", messages.get(0));
        assertEquals(ChatColor.YELLOW + "p1 - Ban - r1", messages.get(1));
        assertEquals(ChatColor.YELLOW + "p2 - Mute - r2", messages.get(2));
    }

    @Test
    public void testJsHistoryCommand() {
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        JSecurityCommand command = new JSecurityCommand(plugin, mock(ConfigManager.class), punishmentManager, mock(PlayerDataManager.class));
        List<PunishmentLogEntry> history = new ArrayList<>();
        history.add(new PunishmentLogEntry("testPlayer", playerUUID, "Warn", "r1", "s1", 0));
        when(punishmentManager.getPlayerHistory(playerUUID)).thenReturn(history);

        command.onCommand(sender, mock(Command.class), "js", new String[]{"history", "testPlayer"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender, times(2)).sendMessage(captor.capture());

        List<String> messages = captor.getAllValues();
        assertEquals(ChatColor.GOLD + "--- History for testPlayer (Page 1/1) ---", messages.get(0));
        assertEquals(ChatColor.YELLOW + "Warn - r1", messages.get(1));
    }
}