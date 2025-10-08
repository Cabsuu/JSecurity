package com.jerae.jsecurity;

import com.jerae.jsecurity.commands.*;
import com.jerae.jsecurity.listeners.PlayerFreezeListener;
import com.jerae.jsecurity.listeners.PlayerListener;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.FreezeManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class JSecurity extends JavaPlugin {

    private ConfigManager configManager;
    private PunishmentManager punishmentManager;
    private FreezeManager freezeManager;

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        punishmentManager = new PunishmentManager(this);
        freezeManager = new FreezeManager();

        // Register commands
        getCommand("ban").setExecutor(new BanCommand(punishmentManager, configManager));
        getCommand("tempban").setExecutor(new TempBanCommand(punishmentManager, configManager));
        getCommand("ipban").setExecutor(new IpBanCommand(punishmentManager, configManager));
        getCommand("mute").setExecutor(new MuteCommand(punishmentManager, configManager));
        getCommand("tempmute").setExecutor(new TempMuteCommand(punishmentManager, configManager));
        getCommand("unban").setExecutor(new UnbanCommand(punishmentManager, configManager));
        getCommand("unmute").setExecutor(new UnmuteCommand(punishmentManager, configManager));
        getCommand("kick").setExecutor(new KickCommand(configManager));
        getCommand("js").setExecutor(new JSecurityCommand(this, configManager));
        getCommand("freeze").setExecutor(new FreezeCommand(freezeManager, configManager));
        getCommand("unfreeze").setExecutor(new UnfreezeCommand(freezeManager, configManager));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(punishmentManager, configManager), this);
        getServer().getPluginManager().registerEvents(new PlayerFreezeListener(freezeManager), this);


        getLogger().info("jSecurity has been enabled.");
    }

    @Override
    public void onDisable() {
        // Save all punishments to file
        if (punishmentManager != null) {
            punishmentManager.savePunishments();
        }
        getLogger().info("jSecurity has been disabled.");
    }
}
