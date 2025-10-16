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
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!configManager.getBoolean("authentication.enabled")) {
            sender.sendMessage("The authentication system is disabled.");
            return true;
        }

        Player player = (Player) sender;
        if (authManager.isRegistered(player.getUniqueId())) {
            player.sendMessage("You are already registered.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("Usage: /register <password> <confirmPassword>");
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];

        if (!password.equals(confirmPassword)) {
            player.sendMessage("Passwords do not match.");
            return true;
        }

        String passwordError = authManager.validatePassword(password);
        if (passwordError != null) {
            player.sendMessage(passwordError);
            return true;
        }

        authManager.registerPlayer(player.getUniqueId(), password);
        player.sendMessage("You have been registered successfully. Please log in using /login <password>");
        System.out.println(player.getName() + " has registered.");
        return true;
    }
}