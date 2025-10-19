package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.VanishManager;
import com.jerae.jsecurity.utils.PermissionUtils;
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
            sender.sendMessage(configManager.getPlayerOnlyCommandMessage());
            return true;
        }

        Player player = (Player) sender;
        if (vanishManager.isVanished(player)) {
            vanishManager.unvanish(player);
            player.sendMessage(configManager.getUnvanishMessage());
        } else {
            vanishManager.vanish(player);
            player.sendMessage(configManager.getVanishMessage());
        }
        return true;
    }
}