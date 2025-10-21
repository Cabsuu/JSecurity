package com.jerae.jsecurity.utils;

import com.jerae.jsecurity.managers.ConfigManager;
import org.bukkit.command.CommandSender;

public class PermissionUtils {

    public static void sendNoPermissionMessage(CommandSender sender, ConfigManager configManager) {
        String noPermissionMessage = ColorUtil.colorize(configManager.getNoPermissionMessage());
        sender.sendMessage(noPermissionMessage);
    }
}