package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand {

    public static void execute(CommandSender sender, ConfigManager configManager) {
        sender.sendMessage(configManager.getJsecurityHelpHeader());
        sender.sendMessage(configManager.getJsecurityHelpHelpMessage());
        sender.sendMessage(configManager.getJsecurityHelpReloadMessage());
        sender.sendMessage(configManager.getJsecurityHelpRecordMessage());
        sender.sendMessage(configManager.getJsecurityHelpProfileMessage());
        sender.sendMessage(configManager.getJsecurityHelpLogMessage());
        sender.sendMessage(configManager.getJsecurityHelpHistoryMessage());
        sender.sendMessage(configManager.getJsecurityHelpNoteMessage());
    }
}