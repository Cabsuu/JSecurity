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

public class BanCommand implements CommandExecutor, TabCompleter {

    private final PunishmentManager punishmentManager;
    private final ConfigManager configManager;

    public BanCommand(PunishmentManager punishmentManager, ConfigManager configManager) {
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /ban <player> [reason] [-s]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || !target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        if (punishmentManager.isBanned(targetUUID)) {
            sender.sendMessage(ChatColor.RED + "That player is already banned.");
            return true;
        }

        boolean silent = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-s"));

        String reason = Arrays.stream(args)
                .skip(1)
                .filter(arg -> !arg.equalsIgnoreCase("-s"))
                .collect(Collectors.joining(" "));

        if (reason.isEmpty()) {
            reason = configManager.getDefaultBanReason();
        }

        String staffName = (sender instanceof Player) ? sender.getName() : "Console";
        String ipAddress = target.isOnline() ? target.getPlayer().getAddress().getAddress().getHostAddress() : null;

        BanEntry ban = new BanEntry(targetUUID, target.getName(), ipAddress, reason, staffName, -1);
        punishmentManager.addBan(ban);

        String kickMessage = configManager.getMessage("kick-messages.ban")
                .replace("{reason}", reason);

        if (target.isOnline()) {
            target.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', kickMessage));
        }

        String broadcastMessage;
        if (silent) {
            broadcastMessage = configManager.getMessage("silent-option.ban-broadcast");
        } else {
            broadcastMessage = configManager.getMessage("ban-broadcast");
        }

        broadcastMessage = broadcastMessage
                .replace("{player}", target.getName())
                .replace("{staff}", staffName)
                .replace("{reason}", reason);

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
            if ("-s".startsWith(args[args.length -1].toLowerCase())) {
                return new ArrayList<>(Arrays.asList("-s"));
            }
        }
        return new ArrayList<>();
    }
}