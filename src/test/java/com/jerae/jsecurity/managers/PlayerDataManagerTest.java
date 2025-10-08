package com.jerae.jsecurity.managers;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.models.PlayerData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PlayerDataManagerTest {

    @TempDir
    File tempDir;

    private JSecurity plugin;
    private PlayerDataManager playerDataManager;
    private File dataFolder;

    @BeforeEach
    void setUp() throws IOException {
        plugin = Mockito.mock(JSecurity.class);
        dataFolder = new File(tempDir, "JSecurity");
        dataFolder.mkdirs();

        when(plugin.getDataFolder()).thenReturn(dataFolder);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

        // Ensure the player_data.json file does not exist before each test
        File playerDataFile = new File(dataFolder, "player_data.json");
        if (playerDataFile.exists()) {
            playerDataFile.delete();
        }

        playerDataManager = new PlayerDataManager(plugin);
    }

    @AfterEach
    void tearDown() {
        // Clean up created files
        File playerDataFile = new File(dataFolder, "player_data.json");
        if (playerDataFile.exists()) {
            playerDataFile.delete();
        }
        dataFolder.delete();
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
        assertEquals(1, retrievedData.getId());
        assertEquals(playerUUID, retrievedData.getUuid());
        assertEquals(playerName, retrievedData.getName());
        assertTrue(retrievedData.getIps().contains(playerIP));
        assertNotNull(retrievedData.getJoined());
    }

    @Test
    void testSaveAndLoadPlayerData() throws IOException {
        UUID player1UUID = UUID.randomUUID();
        String player1Name = "Player1";
        String player1IP = "192.168.1.1";

        UUID player2UUID = UUID.randomUUID();
        String player2Name = "Player2";
        String player2IP = "192.168.1.2";

        // Action: Create and save data for two players
        playerDataManager.createPlayerData(player1UUID, player1Name, player1IP);
        playerDataManager.createPlayerData(player2UUID, player2Name, player2IP);
        playerDataManager.savePlayerData();

        // Verification: Check if the file was created and is not empty
        File playerDataFile = new File(dataFolder, "player_data.json");
        assertTrue(playerDataFile.exists());
        assertTrue(playerDataFile.length() > 0);

        // Action: Create a new manager to force loading from the file
        PlayerDataManager newPlayerDataManager = new PlayerDataManager(plugin);

        // Verification: Check if the data was loaded correctly
        PlayerData loadedData1 = newPlayerDataManager.getPlayerData(player1UUID);
        assertNotNull(loadedData1);
        assertEquals(player1Name, loadedData1.getName());

        PlayerData loadedData2 = newPlayerDataManager.getPlayerData(player2UUID);
        assertNotNull(loadedData2);
        assertEquals(player2Name, loadedData2.getName());
        assertEquals(2, newPlayerDataManager.getAllPlayerData().size());
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
        assertNotNull(playerData);

        playerData.addIp(secondIP);
        playerDataManager.savePlayerData();

        // Action: Reload data
        PlayerDataManager newManager = new PlayerDataManager(plugin);
        PlayerData reloadedData = newManager.getPlayerData(playerUUID);

        // Verification
        assertNotNull(reloadedData);
        assertEquals(2, reloadedData.getIps().size());
        assertTrue(reloadedData.getIps().contains(firstIP));
        assertTrue(reloadedData.getIps().contains(secondIP));
    }
}