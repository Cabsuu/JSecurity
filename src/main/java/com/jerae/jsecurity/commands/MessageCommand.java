package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.MessageManager;
import com.jerae.jsecurity.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageCommand implements CommandExecutor {

    private final MessageManager messageManager;
    private final ConfigManager configManager;

    public MessageCommand(MessageManager messageManager, ConfigManager configManager) {
        this.messageManager = messageManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jsecurity.message")) {
            PermissionUtils.sendNoPermissionMessage(sender, configManager);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can send private messages.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /message <player> <message>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        String message = String.join(" ", args).substring(args[0].length() + 1);
        messageManager.sendMessage((Player) sender, target, message);
        return true;
    }
}