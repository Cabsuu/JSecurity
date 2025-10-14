package com.jerae.jsecurity;

import com.jerae.jsecurity.commands.*;
import com.jerae.jsecurity.listeners.*;
import com.jerae.jsecurity.managers.*;
import com.jerae.jsecurity.utils.LoginCommandFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class JSecurity extends JavaPlugin {

    private ConfigManager configManager;
    private PunishmentManager punishmentManager;
    private FreezeManager freezeManager;
    private PlayerDataManager playerDataManager;
    private MessageManager messageManager;
    private VanishManager vanishManager;
    private AuthManager authManager;
    private LoginCommandFilter filter;

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        punishmentManager = new PunishmentManager(this);
        freezeManager = new FreezeManager();
        playerDataManager = new PlayerDataManager(this);
        messageManager = new MessageManager(configManager);
        vanishManager = new VanishManager(this);
        authManager = new AuthManager(this);

        // Register listeners
        PlayerListener playerListener = new PlayerListener(this, punishmentManager, configManager, playerDataManager, authManager);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(new PlayerFreezeListener(freezeManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(playerDataManager, configManager), this);
        getServer().getPluginManager().registerEvents(new ChatListener(configManager), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this, vanishManager), this);


        // Register commands
        getCommand("ban").setExecutor(new BanCommand(punishmentManager, configManager, playerListener, playerDataManager));
        getCommand("tempban").setExecutor(new TempBanCommand(punishmentManager, configManager, playerListener, playerDataManager));
        getCommand("ipban").setExecutor(new IpBanCommand(punishmentManager, configManager, playerListener, playerDataManager));
        getCommand("mute").setExecutor(new MuteCommand(punishmentManager, configManager));
        getCommand("tempmute").setExecutor(new TempMuteCommand(punishmentManager, configManager));
        getCommand("unban").setExecutor(new UnbanCommand(punishmentManager, configManager));
        getCommand("unmute").setExecutor(new UnmuteCommand(punishmentManager, configManager));
        getCommand("kick").setExecutor(new KickCommand(configManager));
        getCommand("warn").setExecutor(new WarnCommand(punishmentManager, configManager));
        getCommand("jsecurity").setExecutor(new JSecurityCommand(this, configManager, punishmentManager, playerDataManager));
        getCommand("freeze").setExecutor(new FreezeCommand(freezeManager, configManager));
        getCommand("unfreeze").setExecutor(new UnfreezeCommand(freezeManager, configManager));
        getCommand("message").setExecutor(new MessageCommand(messageManager, configManager));
        getCommand("reply").setExecutor(new ReplyCommand(messageManager, configManager));
        getCommand("socialspy").setExecutor(new SocialSpyCommand(messageManager, configManager));
        getCommand("vanish").setExecutor(new VanishCommand(vanishManager, configManager));
        getCommand("register").setExecutor(new RegisterCommand(authManager, configManager));
        getCommand("login").setExecutor(new LoginCommand(authManager, configManager));


        Logger rootLogger = (Logger) LogManager.getRootLogger();
        this.filter = new LoginCommandFilter();
        rootLogger.addFilter(this.filter);

        getLogger().info("jSecurity has been enabled.");
    }

    @Override
    public void onDisable() {
        if (this.filter != null) {
            Logger rootLogger = (Logger) LogManager.getRootLogger();
            rootLogger.getContext().removeFilter(this.filter);
        }
        // Save all punishments to file
        if (punishmentManager != null) {
            punishmentManager.savePunishments();
        }
        if (playerDataManager != null) {
            playerDataManager.savePlayerData();
        }
        getLogger().info("jSecurity has been disabled.");
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
