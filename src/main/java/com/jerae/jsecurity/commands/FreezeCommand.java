package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.FreezeManager;
import com.jerae.jsecurity.managers.FreezeManager;
import com.jerae.jsecurity.utils.ColorUtil;
import com.jerae.jsecurity.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCommand implements CommandExecutor {

    private final FreezeManager freezeManager;
    private final ConfigManager configManager;

    public FreezeCommand(FreezeManager freezeManager, ConfigManager configManager) {
        this.freezeManager = freezeManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jsecurity.freeze")) {
            PermissionUtils.sendNoPermissionMessage(sender, configManager);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(configManager.getFreezeUsageMessage());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(configManager.getPlayerNotFoundMessage());
            return true;
        }

        if (freezeManager.isFrozen(target)) {
            sender.sendMessage(configManager.getAlreadyFrozenMessage(target.getName()));
            return true;
        }

        freezeManager.freeze(target);
        target.sendMessage(ColorUtil.colorize(configManager.getMessage("freeze-message")));
        sender.sendMessage(ColorUtil.colorize(configManager.getFrozenMessage(target.getName())));

        return true;
    }
}