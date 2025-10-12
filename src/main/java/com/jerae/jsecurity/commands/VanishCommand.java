package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.VanishManager;
import com.jerae.jsecurity.utils.PermissionUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor {

    private final VanishManager vanishManager;
    private final ConfigManager configManager;

    public VanishCommand(VanishManager vanishManager, ConfigManager configManager) {
        this.vanishManager = vanishManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jsecurity.vanish")) {
            PermissionUtils.sendNoPermissionMessage(sender, configManager);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use vanish.");
            return true;
        }

        Player player = (Player) sender;
        if (vanishManager.isVanished(player)) {
            vanishManager.unvanish(player);
            player.sendMessage(ChatColor.GREEN + "You are no longer vanished.");
        } else {
            vanishManager.vanish(player);
            player.sendMessage(ChatColor.GREEN + "You are now vanished.");
        }
        return true;
    }
}