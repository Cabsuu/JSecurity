package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.utils.ColorUtil;
import org.bukkit.command.CommandSender;

public class HelpCommand {

    public static void execute(CommandSender sender, ConfigManager configManager) {
        sender.sendMessage(ColorUtil.colorize(configManager.getJsecurityHelpHeader()));
        sender.sendMessage(ColorUtil.colorize(configManager.getJsecurityHelpHelpMessage()));
        sender.sendMessage(ColorUtil.colorize(configManager.getJsecurityHelpReloadMessage()));
        sender.sendMessage(ColorUtil.colorize(configManager.getJsecurityHelpRecordMessage()));
        sender.sendMessage(ColorUtil.colorize(configManager.getJsecurityHelpProfileMessage()));
        sender.sendMessage(ColorUtil.colorize(configManager.getJsecurityHelpLogMessage()));
        sender.sendMessage(ColorUtil.colorize(configManager.getJsecurityHelpHistoryMessage()));
        sender.sendMessage(ColorUtil.colorize(configManager.getJsecurityHelpNoteMessage()));
    }
}