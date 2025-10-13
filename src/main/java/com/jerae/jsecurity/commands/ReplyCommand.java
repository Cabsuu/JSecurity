package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.MessageManager;
import com.jerae.jsecurity.utils.PermissionUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyCommand implements CommandExecutor {

    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public ReplyCommand(MessageManager messageManager, ConfigManager configManager) {
        this.messageManager = messageManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jsecurity.reply")) {
            PermissionUtils.sendNoPermissionMessage(sender, configManager);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can reply to messages.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /reply <message>");
            return true;
        }

        String message = String.join(" ", args);
        messageManager.replyToMessage((Player) sender, message);
        return true;
    }
}