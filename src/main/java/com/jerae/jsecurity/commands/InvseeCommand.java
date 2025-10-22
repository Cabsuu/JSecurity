package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.InvseeManager;
import com.jerae.jsecurity.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InvseeCommand implements CommandExecutor {

    private final InvseeManager invseeManager;
    private final ConfigManager configManager;

    public InvseeCommand(InvseeManager invseeManager, ConfigManager configManager) {
        this.invseeManager = invseeManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(configManager.getPlayerOnlyCommandMessage());
            return true;
        }

        Player staff = (Player) sender;
        if (!staff.hasPermission("jsecurity.invsee")) {
            staff.sendMessage(configManager.getNoPermissionMessage());
            return true;
        }

        if (args.length != 1) {
            staff.sendMessage(configManager.getInvseeUsageMessage());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            staff.sendMessage(configManager.getPlayerNotFoundMessage());
            return true;
        }

        invseeManager.openInventory(staff, target);
        return true;
    }
}
