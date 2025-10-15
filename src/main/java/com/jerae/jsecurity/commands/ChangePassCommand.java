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
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!configManager.getBoolean("authentication.enabled")) {
            sender.sendMessage("The authentication system is disabled.");
            return true;
        }

        Player player = (Player) sender;
        if (!authManager.isRegistered(player.getUniqueId())) {
            player.sendMessage("You are not registered.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("Usage: /changepass <oldPassword> <newPassword>");
            return true;
        }

        String oldPassword = args[0];
        String newPassword = args[1];

        if (!authManager.checkPassword(player.getUniqueId(), oldPassword)) {
            player.sendMessage("Incorrect old password.");
            return true;
        }

        String passwordError = authManager.validatePassword(newPassword);
        if (passwordError != null) {
            player.sendMessage(passwordError);
            return true;
        }

        authManager.changePassword(player.getUniqueId(), newPassword);
        player.sendMessage("Your password has been changed successfully.");
        return true;
    }
}