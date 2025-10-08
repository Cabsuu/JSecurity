package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UnbanCommand implements CommandExecutor, TabCompleter {

    private final PunishmentManager punishmentManager;
    private final ConfigManager configManager;

    public UnbanCommand(PunishmentManager punishmentManager, ConfigManager configManager) {
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unban <player/ip> [-s]");
            return true;
        }

        String targetIdentifier = args[0];
        String staffName = (sender instanceof Player) ? sender.getName() : "Console";
        boolean silent = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-s"));

        BanEntry banEntry = null;
        OfflinePlayer targetPlayer = null;

        // Check if it's an IP address
        if (targetIdentifier.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            banEntry = punishmentManager.getBanByIp(targetIdentifier);
            if (banEntry != null) {
                targetPlayer = Bukkit.getOfflinePlayer(banEntry.getUuid());
            }
        } else {
            targetPlayer = Bukkit.getOfflinePlayer(targetIdentifier);
            if (targetPlayer != null) {
                banEntry = punishmentManager.getBan(targetPlayer.getUniqueId());
            }
        }

        if (targetPlayer == null || banEntry == null) {
            sender.sendMessage(ChatColor.RED + "That player or IP is not banned.");
            return true;
        }

        punishmentManager.removeBan(banEntry.getUuid());

        String targetName = targetPlayer.getName() != null ? targetPlayer.getName() : targetIdentifier;

        if (!silent) {
            String broadcastMessage = configManager.getMessage("unban-broadcast")
                    .replace("{player}", targetName)
                    .replace("{staff}", staffName);
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMessage));
        }

        sender.sendMessage(ChatColor.GREEN + "Successfully unbanned " + targetName + ".");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // This is a bit tricky as we need to list banned players, not online ones.
            // For simplicity, we'll just suggest online players for now. A more complex
            // implementation would require getting a list of banned player names from the PunishmentManager.
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}