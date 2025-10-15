package com.jerae.jsecurity.managers;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.models.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PlayerDataManagerTest {

    @TempDir
    File tempDir;

    private JSecurity plugin;
    private PlayerDataManager playerDataManager;
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
    }

    @AfterEach
    void tearDown() {
        databaseManager.close();
    }

    @Test
    void testCreateAndGetPlayerData() {
        UUID playerUUID = UUID.randomUUID();
        String playerName = "TestPlayer";
        String playerIP = "127.0.0.1";

        // Pre-condition: No data should exist for the player
        assertNull(playerDataManager.getPlayerData(playerUUID));

        // Action: Create player data
        playerDataManager.createPlayerData(playerUUID, playerName, playerIP);

        // Verification: Player data should now exist
        PlayerData retrievedData = playerDataManager.getPlayerData(playerUUID);
        assertNotNull(retrievedData);
        assertEquals(playerUUID, retrievedData.getUuid());
        assertEquals(playerName, retrievedData.getName());
        assertTrue(retrievedData.getIps().contains(playerIP));
        assertNotNull(retrievedData.getJoined());
    }

    @Test
    void testAddIpToExistingPlayer() {
        UUID playerUUID = UUID.randomUUID();
        String playerName = "TestPlayer";
        String firstIP = "1.1.1.1";
        String secondIP = "2.2.2.2";

        // Action: Create player and add a new IP
        playerDataManager.createPlayerData(playerUUID, playerName, firstIP);
        PlayerData playerData = playerDataManager.getPlayerData(playerUUID);
        playerData.addIp(secondIP);
        playerDataManager.updatePlayerData(playerData);

        // Verification
        PlayerData reloadedData = playerDataManager.getPlayerData(playerUUID);
        assertNotNull(reloadedData);
        assertEquals(2, reloadedData.getIps().size());
        assertTrue(reloadedData.getIps().contains(firstIP));
        assertTrue(reloadedData.getIps().contains(secondIP));
    }
}