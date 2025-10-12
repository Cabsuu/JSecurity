package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.AuthManager;
import com.jerae.jsecurity.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {

    private final AuthManager authManager;
    private final ConfigManager configManager;

    public LoginCommand(AuthManager authManager, ConfigManager configManager) {
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
        if (authManager.isLoggedIn(player)) {
            player.sendMessage("You are already logged in.");
            return true;
        }

        if (!authManager.isRegistered(player.getUniqueId())) {
            player.sendMessage("You are not registered. Please register using /register <password> <confirmPassword>");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("Usage: /login <password>");
            return true;
        }

        String password = args[0];
        if (authManager.checkPassword(player.getUniqueId(), password)) {
            authManager.loginPlayer(player);
            player.sendMessage("You have logged in successfully.");
        } else {
            player.sendMessage("Incorrect password.");
        }
        return true;
    }
}