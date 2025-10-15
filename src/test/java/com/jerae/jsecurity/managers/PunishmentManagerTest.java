package com.jerae.jsecurity.managers;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.models.PunishmentLogEntry;
import com.jerae.jsecurity.models.WarnEntry;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PunishmentManagerTest {

    @TempDir
    File tempDir;

    private JSecurity plugin;
    private PlayerDataManager playerDataManager;
    private PunishmentManager punishmentManager;
    private DatabaseManager databaseManager;
    private File dataFolder;

    @BeforeEach
    void setUp() throws IOException {
        plugin = Mockito.mock(JSecurity.class);
        dataFolder = new File(tempDir, "JSecurity");
        dataFolder.mkdirs();

        when(plugin.getDataFolder()).thenReturn(dataFolder);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

        FileConfiguration config = Mockito.mock(FileConfiguration.class);
        when(config.getString("database.type", "sqlite")).thenReturn("sqlite");
        when(plugin.getConfig()).thenReturn(config);

        databaseManager = new DatabaseManager(plugin);
        playerDataManager = new PlayerDataManager(plugin, databaseManager);
        punishmentManager = new PunishmentManager(plugin, databaseManager);
    }

    @AfterEach
    void tearDown() {
        databaseManager.close();
    }

    @Test
    void testBanAndUnban() {
        UUID playerUUID = UUID.randomUUID();
        playerDataManager.createPlayerData(playerUUID, "testPlayer", "127.0.0.1");
        BanEntry ban = new BanEntry(playerUUID, "testPlayer", "127.0.0.1", "test reason", "staff", -1);

        punishmentManager.addBan(ban);
        assertTrue(punishmentManager.isBanned(playerUUID));

        punishmentManager.removeBan(playerUUID);
        assertFalse(punishmentManager.isBanned(playerUUID));
    }

    @Test
    void testMuteAndUnmute() {
        UUID playerUUID = UUID.randomUUID();
        playerDataManager.createPlayerData(playerUUID, "testPlayer", "127.0.0.1");
        MuteEntry mute = new MuteEntry(playerUUID, "testPlayer", "test reason", "staff", -1);

        punishmentManager.addMute(mute);
        assertTrue(punishmentManager.isMuted(playerUUID));

        punishmentManager.removeMute(playerUUID);
        assertFalse(punishmentManager.isMuted(playerUUID));
    }

    @Test
    void testWarn() {
        UUID playerUUID = UUID.randomUUID();
        String playerName = "testPlayer";
        playerDataManager.createPlayerData(playerUUID, playerName, "127.0.0.1");
        WarnEntry warn = new WarnEntry(playerUUID, playerName, "test reason", "staff");

        punishmentManager.addWarn(warn);
        List<PunishmentLogEntry> history = punishmentManager.getPlayerHistory(playerUUID);

        assertFalse(history.isEmpty());
        assertEquals("warn", history.get(0).getPunishmentType());
    }
}