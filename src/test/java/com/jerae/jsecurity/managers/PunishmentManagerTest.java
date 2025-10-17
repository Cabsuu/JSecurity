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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    void testBanAndUnban() throws InterruptedException {
        UUID playerUUID = UUID.randomUUID();
        playerDataManager.createPlayerData(playerUUID, "testPlayer", "127.0.0.1");
        Thread.sleep(100); // Wait for async create to finish
        BanEntry ban = new BanEntry(playerUUID, "testPlayer", "127.0.0.1", "test reason", "staff", -1);

        punishmentManager.addBan(ban);
        Thread.sleep(100); // Wait for async addBan to finish
        assertTrue(punishmentManager.isBanned(playerUUID));

        punishmentManager.removeBan(playerUUID);
        Thread.sleep(100); // Wait for async removeBan to finish
        assertFalse(punishmentManager.isBanned(playerUUID));
    }

    @Test
    void testMuteAndUnmute() throws InterruptedException {
        UUID playerUUID = UUID.randomUUID();
        playerDataManager.createPlayerData(playerUUID, "testPlayer", "127.0.0.1");
        Thread.sleep(100); // Wait for async create to finish
        MuteEntry mute = new MuteEntry(playerUUID, "testPlayer", "test reason", "staff", -1);

        punishmentManager.addMute(mute);
        Thread.sleep(100); // Wait for async addMute to finish
        assertTrue(punishmentManager.isMuted(playerUUID));

        punishmentManager.removeMute(playerUUID);
        Thread.sleep(100); // Wait for async removeMute to finish
        assertFalse(punishmentManager.isMuted(playerUUID));
    }

    @Test
    void testWarn() throws InterruptedException {
        UUID playerUUID = UUID.randomUUID();
        String playerName = "testPlayer";
        playerDataManager.createPlayerData(playerUUID, playerName, "127.0.0.1");
        Thread.sleep(100); // Wait for async create to finish
        WarnEntry warn = new WarnEntry(playerUUID, playerName, "test reason", "staff");

        punishmentManager.addWarn(warn);
        Thread.sleep(100); // Wait for async addWarn to finish
        List<PunishmentLogEntry> history = punishmentManager.getPlayerHistory(playerUUID);

        assertFalse(history.isEmpty());
        assertEquals("warn", history.get(0).getPunishmentType());
    }

    @Test
    void testConcurrentWarns() throws InterruptedException {
        int numberOfThreads = 10;
        int warnsPerThread = 20;
        int totalWarns = numberOfThreads * warnsPerThread;
        UUID playerUUID = UUID.randomUUID();
        String playerName = "testPlayer";

        playerDataManager.createPlayerData(playerUUID, playerName, "127.0.0.1");

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(totalWarns);

        for (int i = 0; i < totalWarns; i++) {
            final int warnId = i;
            executor.submit(() -> {
                try {
                    WarnEntry warn = new WarnEntry(playerUUID, playerName, "Concurrent warn " + warnId, "TestThread");
                    punishmentManager.addWarn(warn);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "Not all warn tasks completed in time");
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Executor did not terminate in time");

        // Allow some time for the async writes to complete
        Thread.sleep(2000);

        List<PunishmentLogEntry> history = punishmentManager.getPlayerHistory(playerUUID);
        assertEquals(totalWarns, history.size(), "Not all warnings were persisted to the database.");
    }
}