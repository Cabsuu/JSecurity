package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.StaffChatManager;
import com.jerae.jsecurity.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChatCommand implements CommandExecutor {

    private final StaffChatManager staffChatManager;
    private final ConfigManager configManager;

    public StaffChatCommand(StaffChatManager staffChatManager, ConfigManager configManager) {
        this.staffChatManager = staffChatManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("jsecurity.staffchat")) {
            player.sendMessage(ColorUtil.colorize(configManager.getNoPermissionMessage()));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ColorUtil.colorize(configManager.getStaffChatUsageMessage()));
            return true;
        }

        String firstArg = args[0];
        if (firstArg.equalsIgnoreCase("toggle")) {
            staffChatManager.toggleStaffChat(player.getUniqueId());
            boolean isInStaffChat = staffChatManager.isInStaffChat(player.getUniqueId());
            player.sendMessage(ColorUtil.colorize(configManager.getStaffChatToggleMessage(isInStaffChat)));
        } else {
            String message = String.join(" ", args);
            String format = configManager.getStaffChatMessageFormat()
                    .replace("{player}", player.getName())
                    .replace("{message}", message);

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("jsecurity.staffchat")) {
                    onlinePlayer.sendMessage(ColorUtil.colorize(format));
                }
            }
        }
        return true;
    }
}