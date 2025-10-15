package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.AuthManager;
import com.jerae.jsecurity.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UnregisterCommand implements CommandExecutor {

    private final AuthManager authManager;
    private final ConfigManager configManager;
    private final Map<UUID, Long> confirmationMap = new HashMap<>();

    public UnregisterCommand(AuthManager authManager, ConfigManager configManager) {
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

        if (args.length < 1) {
            player.sendMessage("Usage: /unregister <password>");
            return true;
        }

        String password = args[0];
        if (!authManager.checkPassword(player.getUniqueId(), password)) {
            player.sendMessage("Incorrect password.");
            return true;
        }

        UUID playerUUID = player.getUniqueId();
        if (confirmationMap.containsKey(playerUUID) && (System.currentTimeMillis() - confirmationMap.get(playerUUID)) < 60000) {
            authManager.unregisterPlayer(playerUUID);
            player.sendMessage("You have been unregistered successfully.");
            confirmationMap.remove(playerUUID);
        } else {
            player.sendMessage("Are you sure you want to unregister? This action cannot be undone. Re-enter the command within 60 seconds to confirm.");
            confirmationMap.put(playerUUID, System.currentTimeMillis());
        }

        return true;
    }
}