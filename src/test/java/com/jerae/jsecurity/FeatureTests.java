package com.jerae.jsecurity;

import com.jerae.jsecurity.commands.*;
import com.jerae.jsecurity.managers.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class FeatureTests {

    private MockedStatic<Bukkit> bukkit;
    private Server server;
    private Player player;
    private CommandSender sender;
    private final UUID playerUUID = UUID.randomUUID();

    @BeforeEach
    public void setUp() {
        server = mock(Server.class);
        player = mock(Player.class);
        sender = mock(CommandSender.class);

        when(player.getName()).thenReturn("testPlayer");
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(sender.getName()).thenReturn("sender");

        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getServer).thenReturn(server);
        bukkit.when(() -> Bukkit.getPlayer(eq("testPlayer"))).thenReturn(player);
        bukkit.when(() -> Bukkit.getOfflinePlayer(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
            when(offlinePlayer.getUniqueId()).thenReturn(UUID.nameUUIDFromBytes(name.getBytes()));
            when(offlinePlayer.getName()).thenReturn(name);
            when(offlinePlayer.hasPlayedBefore()).thenReturn(true);
            return offlinePlayer;
        });
    }

    @AfterEach
    public void tearDown() {
        bukkit.close();
    }

    @Test
    public void testKickCommand() {
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getMessage(anyString())).thenReturn("Kicked for a reason.");
        KickCommand kickCommand = new KickCommand(configManager);
        kickCommand.onCommand(sender, mock(Command.class), "kick", new String[]{"testPlayer", "test reason"});
        verify(player).kick(any(Component.class));
        bukkit.verify(() -> Bukkit.broadcastMessage(anyString()));
    }

    @Test
    public void testKickCommandSilent() {
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getMessage(anyString())).thenReturn("Kicked silently.");
        KickCommand kickCommand = new KickCommand(configManager);
        kickCommand.onCommand(sender, mock(Command.class), "kick", new String[]{"testPlayer", "test reason", "-s"});
        verify(player).kick(any(Component.class));
        bukkit.verify(() -> Bukkit.broadcastMessage(anyString()), never());
    }

    @Test
    public void testUnbanCommandSilent() {
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        when(punishmentManager.getBan(any(UUID.class))).thenReturn(mock(BanEntry.class));
        ConfigManager configManager = mock(ConfigManager.class);
        UnbanCommand unbanCommand = new UnbanCommand(punishmentManager, configManager);
        unbanCommand.onCommand(sender, mock(Command.class), "unban", new String[]{"testPlayer", "-s"});
        verify(punishmentManager).removeBan(any());
        bukkit.verify(() -> Bukkit.broadcastMessage(anyString()), never());
    }

    @Test
    public void testUnmuteCommandSilent() {
        PunishmentManager punishmentManager = mock(PunishmentManager.class);
        when(punishmentManager.getMute(any(UUID.class))).thenReturn(mock(MuteEntry.class));
        ConfigManager configManager = mock(ConfigManager.class);
        UnmuteCommand unmuteCommand = new UnmuteCommand(punishmentManager, configManager);
        unmuteCommand.onCommand(sender, mock(Command.class), "unmute", new String[]{"testPlayer", "-s"});
        verify(punishmentManager).removeMute(any());
        bukkit.verify(() -> Bukkit.broadcastMessage(anyString()), never());
    }

    @Test
    public void testReloadCommand() {
        JSecurity plugin = mock(JSecurity.class);
        ConfigManager configManager = mock(ConfigManager.class);
        JSecurityCommand jSecurityCommand = new JSecurityCommand(plugin, configManager);
        jSecurityCommand.onCommand(sender, mock(Command.class), "js", new String[]{"reload"});
        verify(configManager).reloadConfig();
        verify(sender).sendMessage(contains("reloaded"));
    }

    @Test
    public void testFreezeCommand() {
        FreezeManager freezeManager = new FreezeManager();
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getMessage("freeze-message")).thenReturn("You are frozen.");
        FreezeCommand freezeCommand = new FreezeCommand(freezeManager, configManager);
        freezeCommand.onCommand(sender, mock(Command.class), "freeze", new String[]{"testPlayer"});
        assertTrue(freezeManager.isFrozen(player));
        verify(sender).sendMessage(contains("You have frozen"));
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
        verify(sender).sendMessage(contains("You have unfrozen"));
    }
}