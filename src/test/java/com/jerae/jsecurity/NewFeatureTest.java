package com.jerae.jsecurity;

import com.jerae.jsecurity.commands.FreezeCommand;
import com.jerae.jsecurity.commands.JSecurityCommand;
import com.jerae.jsecurity.commands.WarnCommand;
import com.jerae.jsecurity.listeners.PlayerListener;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.FreezeManager;
import com.jerae.jsecurity.managers.PlayerDataManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.models.PunishmentLogEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NewFeatureTest {

    private MockedStatic<Bukkit> bukkit;
    private Server server;
    private Player player;
    private CommandSender sender;
    private final UUID playerUUID = UUID.randomUUID();
    private OfflinePlayer offlinePlayer;
    private ConsoleCommandSender console;

    @BeforeEach
    public void setUp() {
        server = mock(Server.class);
        player = mock(Player.class);
        sender = mock(CommandSender.class);
        offlinePlayer = mock(OfflinePlayer.class);
        console = mock(ConsoleCommandSender.class);

        when(player.getName()).thenReturn("testPlayer");
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(sender.getName()).thenReturn("sender");

        when(offlinePlayer.getName()).thenReturn("testPlayer");
        when(offlinePlayer.getUniqueId()).thenReturn(playerUUID);
        when(offlinePlayer.hasPlayedBefore()).thenReturn(true);
        when(offlinePlayer.isOnline()).thenReturn(true);
        when(offlinePlayer.getPlayer()).thenReturn(player);

        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getServer).thenReturn(server);
        bukkit.when(() -> Bukkit.getPlayer("testPlayer")).thenReturn(player);
        bukkit.when(() -> Bukkit.getOfflinePlayer("testPlayer")).thenReturn(offlinePlayer);
        bukkit.when(Bukkit::getConsoleSender).thenReturn(console);
    }

    @AfterEach
    public void tearDown() {
        bukkit.close();
    }

    @Test
    public void testWarnCommand() {
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getMessage(eq("warn-message"), anyBoolean())).thenReturn("You have been warned for {reason}.");
        when(configManager.getMessage(eq("warn-broadcast"), anyBoolean())).thenReturn("{player} has been warned by {staff} for {reason}.");
        when(configManager.getMessage(eq("warn-sender"), anyBoolean())).thenReturn("You have warned {player} for {reason}.");

        WarnCommand warnCommand = new WarnCommand(punishmentManager, configManager);
        warnCommand.onCommand(sender, mock(Command.class), "warn", new String[]{"testPlayer", "test reason"});

        verify(offlinePlayer).isOnline();
        verify(offlinePlayer).getPlayer();
        ArgumentCaptor<Component> playerMessageCaptor = ArgumentCaptor.forClass(Component.class);
        verify(player).sendMessage(playerMessageCaptor.capture());
        assertEquals("You have been warned for test reason.", componentToString(playerMessageCaptor.getValue()));

        ArgumentCaptor<Component> broadcastMessageCaptor = ArgumentCaptor.forClass(Component.class);
        verify(server).broadcast(broadcastMessageCaptor.capture());
        assertEquals("testPlayer has been warned by sender for test reason.", componentToString(broadcastMessageCaptor.getValue()));

        ArgumentCaptor<Component> senderMessageCaptor = ArgumentCaptor.forClass(Component.class);
        verify(sender).sendMessage(senderMessageCaptor.capture());
        assertEquals("You have warned testPlayer for test reason.", componentToString(senderMessageCaptor.getValue()));
    }

    @Test
    public void testFreezeAlreadyFrozen() {
        FreezeManager freezeManager = new FreezeManager();
        ConfigManager configManager = mock(ConfigManager.class);
        freezeManager.freeze(player);

        FreezeCommand freezeCommand = new FreezeCommand(freezeManager, configManager);
        freezeCommand.onCommand(sender, mock(Command.class), "freeze", new String[]{"testPlayer"});

        verify(sender).sendMessage(ChatColor.RED + "testPlayer is already frozen.");
        verify(player, never()).sendMessage(any(String.class));
    }

    @Test
    public void testJsLogWithDate() {
        JSecurity plugin = mock(JSecurity.class);
        ConfigManager configManager = mock(ConfigManager.class);
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        PlayerDataManager playerDataManager = mock(PlayerDataManager.class);
        long timestamp = System.currentTimeMillis();
        LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        when(punishmentManager.getPunishmentLogs()).thenReturn(Collections.singletonList(
                new PunishmentLogEntry("testPlayer", playerUUID, "warn", "test reason", "sender", timestamp)
        ));

        JSecurityCommand jSecurityCommand = new JSecurityCommand(plugin, configManager, punishmentManager, playerDataManager);
        jSecurityCommand.onCommand(sender, mock(Command.class), "js", new String[]{"log"});

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(sender, times(2)).sendMessage(captor.capture());

        List<String> messages = captor.getAllValues();
        assertTrue(messages.get(1).contains("[" + date.format(formatter) + "]"));
    }

    @Test
    public void testAltAccountAlertConsolidated() throws Exception {
        // --- Setup ---
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        ConfigManager configManager = mock(ConfigManager.class);
        PlayerListener listener = new PlayerListener(punishmentManager, configManager);

        InetSocketAddress sharedAddress = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 12345);

        // Player who is joining
        Player joiningPlayer = mock(Player.class);
        when(joiningPlayer.getName()).thenReturn("JoiningPlayer");
        when(joiningPlayer.getSocketAddress()).thenReturn(sharedAddress);

        // Other players with the same IP
        Player altPlayer1 = mock(Player.class);
        when(altPlayer1.getName()).thenReturn("AltPlayer1");
        when(altPlayer1.getSocketAddress()).thenReturn(sharedAddress);

        Player altPlayer2 = mock(Player.class);
        when(altPlayer2.getName()).thenReturn("AltPlayer2");
        when(altPlayer2.getSocketAddress()).thenReturn(sharedAddress);

        // Staff player to receive alert
        Player staffPlayer = mock(Player.class);
        when(staffPlayer.getName()).thenReturn("StaffPlayer");
        when(staffPlayer.hasPermission("jsecurity.alt.alert")).thenReturn(true);
        // Give staff a different address to avoid including them in the alt list
        when(staffPlayer.getSocketAddress()).thenReturn(new InetSocketAddress(InetAddress.getByName("1.2.3.4"), 54321));

        // Mock online players
        bukkit.when(Bukkit::getOnlinePlayers).thenReturn(List.of(joiningPlayer, altPlayer1, altPlayer2, staffPlayer));
        when(configManager.isAltAccountAlertEnabled()).thenReturn(true);
        when(configManager.getMessage("alt-account-alert")).thenReturn("Alt accounts on {player}'s IP: {alt_list}");

        // --- Action ---
        PlayerJoinEvent event = new PlayerJoinEvent(joiningPlayer, "");
        listener.onPlayerJoin(event);

        // --- Verification ---
        ArgumentCaptor<String> consoleCaptor = ArgumentCaptor.forClass(String.class);
        verify(console).sendMessage(consoleCaptor.capture());

        ArgumentCaptor<String> staffCaptor = ArgumentCaptor.forClass(String.class);
        verify(staffPlayer).sendMessage(staffCaptor.capture());

        String expectedMessage = "Alt accounts on JoiningPlayer's IP: JoiningPlayer, AltPlayer1, AltPlayer2";
        assertEquals(expectedMessage, consoleCaptor.getValue());
        assertEquals(ChatColor.translateAlternateColorCodes('&', expectedMessage), staffCaptor.getValue());
    }


    private String componentToString(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}