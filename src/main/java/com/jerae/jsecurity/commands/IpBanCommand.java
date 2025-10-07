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
import java.util.UUID;
import java.util.stream.Collectors;

public class IpBanCommand implements CommandExecutor, TabCompleter {

    private final PunishmentManager punishmentManager;
    private final ConfigManager configManager;

    public IpBanCommand(PunishmentManager punishmentManager, ConfigManager configManager) {
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /ipban <player/ip> [-s]");
            return true;
        }

        String targetIdentifier = args[0];
        boolean silent = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-s"));
        String staffName = (sender instanceof Player) ? sender.getName() : "Console";

        Player targetPlayer = Bukkit.getPlayer(targetIdentifier);

        // If target is an IP address
        if (targetPlayer == null && targetIdentifier.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String ip = targetIdentifier;
            if (punishmentManager.isIpBanned(ip)) {
                sender.sendMessage(ChatColor.RED + "That IP is already banned.");
                return true;
            }

            // Find an online player with this IP
            Player playerToBan = null;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getAddress().getAddress().getHostAddress().equals(ip)) {
                    playerToBan = onlinePlayer;
                    break;
                }
            }

            if (playerToBan == null) {
                sender.sendMessage(ChatColor.RED + "No online player found with that IP. You can ban an offline player with /ban <player>.");
                // Or we could implement a way to ban an IP without a player, but this is safer.
                return true;
            }
            targetPlayer = playerToBan;
        } else if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or invalid IP address.");
            return true;
        }

        if (punishmentManager.isBanned(targetPlayer.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "That player is already banned.");
            return true;
        }

        String ipAddress = targetPlayer.getAddress().getAddress().getHostAddress();
        String reason = "IP Ban"; // IP bans generally don't need a detailed reason.

        BanEntry ban = new BanEntry(targetPlayer.getUniqueId(), targetPlayer.getName(), ipAddress, reason, staffName, -1);
        punishmentManager.addBan(ban);

        String kickMessage = configManager.getMessage("kick-messages.ipban");
        targetPlayer.kickPlayer(ChatColor.translateAlternateColorCodes('&', kickMessage));

        String broadcastMessage;
        if (silent) {
            broadcastMessage = configManager.getMessage("silent-option.ipban-broadcast");
        } else {
            broadcastMessage = configManager.getMessage("ipban-broadcast");
        }

        broadcastMessage = broadcastMessage
                .replace("{player}", targetPlayer.getName())
                .replace("{staff}", staffName);

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMessage));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length > 1) {
            if ("-s".startsWith(args[args.length - 1].toLowerCase())) {
                return new ArrayList<>(Arrays.asList("-s"));
            }
        }
        return new ArrayList<>();
    }
}