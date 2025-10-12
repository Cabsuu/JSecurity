package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.MessageManager;
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
            sender.sendMessage(configManager.getNoPermissionMessage());
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use social spy.");
            return true;
        }

        Player player = (Player) sender;
        if (messageManager.toggleSocialSpy(player)) {
            player.sendMessage(ChatColor.GREEN + "Social spy enabled.");
        } else {
            player.sendMessage(ChatColor.RED + "Social spy disabled.");
        }
        return true;
    }
}