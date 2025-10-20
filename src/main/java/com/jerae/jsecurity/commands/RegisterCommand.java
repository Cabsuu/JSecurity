package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.AuthManager;
import com.jerae.jsecurity.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {

    private final AuthManager authManager;
    private final ConfigManager configManager;

    public RegisterCommand(AuthManager authManager, ConfigManager configManager) {
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
        if (authManager.isRegistered(player.getUniqueId())) {
            player.sendMessage(configManager.getAlreadyRegisteredMessage());
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(configManager.getRegisterUsageMessage());
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];

        if (!password.equals(confirmPassword)) {
            player.sendMessage(configManager.getPasswordMismatchMessage());
            return true;
        }

        String passwordError = authManager.validatePassword(password);
        if (passwordError != null) {
            player.sendMessage(passwordError);
            return true;
        }

        authManager.registerPlayer(player.getUniqueId(), password);
        player.sendMessage(configManager.getRegisterSuccessMessage());
        System.out.println(player.getName() + " has registered.");
        return true;
    }
}