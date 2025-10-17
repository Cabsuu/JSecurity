package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.FreezeManager;
import com.jerae.jsecurity.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnfreezeCommand implements CommandExecutor {

    private final FreezeManager freezeManager;
    private final ConfigManager configManager;

    public UnfreezeCommand(FreezeManager freezeManager, ConfigManager configManager) {
        this.freezeManager = freezeManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jsecurity.unfreeze")) {
            PermissionUtils.sendNoPermissionMessage(sender, configManager);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(configManager.getUnfreezeUsageMessage());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(configManager.getPlayerNotFoundMessage());
            return true;
        }

        if (!freezeManager.isFrozen(target)) {
            sender.sendMessage(configManager.getNotFrozenMessage(target.getName()));
            return true;
        }

        freezeManager.unfreeze(target);
        target.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("unfreeze-message")));
        sender.sendMessage(configManager.getUnfrozenMessage(target.getName()));

        return true;
    }
}