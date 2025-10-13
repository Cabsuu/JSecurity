package com.jerae.jsecurity.utils;

import com.jerae.jsecurity.managers.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public class PermissionUtils {

    public static void sendNoPermissionMessage(CommandSender sender, ConfigManager configManager) {
        Component noPermissionMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(configManager.getNoPermissionMessage());
        sender.sendMessage(noPermissionMessage);
    }
}