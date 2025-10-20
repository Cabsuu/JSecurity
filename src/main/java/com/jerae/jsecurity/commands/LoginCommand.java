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
            sender.sendMessage(configManager.getPlayerOnlyCommandMessage());
            return true;
        }

        if (!configManager.isAuthEnabled()) {
            sender.sendMessage(configManager.getAuthDisabledMessage());
            return true;
        }

        Player player = (Player) sender;
        if (authManager.isLoggedIn(player)) {
            player.sendMessage(configManager.getAlreadyLoggedInMessage());
            return true;
        }

        if (!authManager.isRegistered(player.getUniqueId())) {
            player.sendMessage(configManager.getNotRegisteredMessage());
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(configManager.getLoginUsageMessage());
            return true;
        }

        String password = args[0];
        if (authManager.checkPassword(player.getUniqueId(), password)) {
            authManager.loginPlayer(player);
            player.sendMessage(configManager.getLoginSuccessMessage());
            System.out.println(player.getName() + " has logged in.");
        } else {
            player.sendMessage(configManager.getLoginFailMessage());
        }
        return true;
    }
}