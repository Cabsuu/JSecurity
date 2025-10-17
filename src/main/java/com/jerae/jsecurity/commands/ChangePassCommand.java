package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.AuthManager;
import com.jerae.jsecurity.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePassCommand implements CommandExecutor {

    private final AuthManager authManager;
    private final ConfigManager configManager;

    public ChangePassCommand(AuthManager authManager, ConfigManager configManager) {
        this.authManager = authManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(configManager.getPlayerOnlyCommandMessage());
            return true;
        }

        if (!configManager.isAuthEnabled()) {
            sender.sendMessage(configManager.getAuthDisabledMessage());
            return true;
        }

        Player player = (Player) sender;
        if (!authManager.isRegistered(player.getUniqueId())) {
            player.sendMessage(configManager.getNotRegisteredMessage());
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(configManager.getChangePassUsageMessage());
            return true;
        }

        String oldPassword = args[0];
        String newPassword = args[1];

        if (!authManager.checkPassword(player.getUniqueId(), oldPassword)) {
            player.sendMessage(configManager.getChangePassFailMessage());
            return true;
        }

        String passwordError = authManager.validatePassword(newPassword);
        if (passwordError != null) {
            player.sendMessage(passwordError);
            return true;
        }

        authManager.changePassword(player.getUniqueId(), newPassword);
        player.sendMessage(configManager.getChangePassSuccessMessage());
        return true;
    }
}