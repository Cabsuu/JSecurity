package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.MessageManager;
import com.jerae.jsecurity.utils.PermissionUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SocialSpyCommand implements CommandExecutor {

    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public SocialSpyCommand(MessageManager messageManager, ConfigManager configManager) {
        this.messageManager = messageManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jsecurity.socialspy")) {
            PermissionUtils.sendNoPermissionMessage(sender, configManager);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(configManager.getPlayerOnlyCommandMessage());
            return true;
        }

        Player player = (Player) sender;
        if (messageManager.toggleSocialSpy(player)) {
            player.sendMessage(configManager.getSocialSpyEnabledMessage());
        } else {
            player.sendMessage(configManager.getSocialSpyDisabledMessage());
        }
        return true;
    }
}