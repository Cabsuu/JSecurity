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

        if (args.length < 1) {
            player.sendMessage(configManager.getUnregisterUsageMessage());
            return true;
        }

        String password = args[0];
        if (!authManager.checkPassword(player.getUniqueId(), password)) {
            player.sendMessage(configManager.getUnregisterFailMessage());
            return true;
        }

        UUID playerUUID = player.getUniqueId();
        if (confirmationMap.containsKey(playerUUID) && (System.currentTimeMillis() - confirmationMap.get(playerUUID)) < 60000) {
            authManager.unregisterPlayer(playerUUID);
            authManager.setUnauthenticated(player);
            player.sendMessage(configManager.getUnregisterSuccessMessage());
            System.out.println(player.getName() + " has unregistered.");
            confirmationMap.remove(playerUUID);
        } else {
            player.sendMessage(configManager.getUnregisterConfirmMessage());
            confirmationMap.put(playerUUID, System.currentTimeMillis());
        }

        return true;
    }
}