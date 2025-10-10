package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.FreezeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCommand implements CommandExecutor {

    private final FreezeManager freezeManager;
    private final ConfigManager configManager;

    public FreezeCommand(FreezeManager freezeManager, ConfigManager configManager) {
        this.freezeManager = freezeManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /freeze <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (freezeManager.isFrozen(target)) {
            sender.sendMessage(ChatColor.RED + target.getName() + " is already frozen.");
            return true;
        }

        freezeManager.freeze(target);
        target.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getMessage("freeze-message")));
        sender.sendMessage(ChatColor.GREEN + "You have frozen " + target.getName() + ".");

        return true;
    }
}