package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.utils.TimeUtil;
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

public class TempBanCommand implements CommandExecutor, TabCompleter {

    private final PunishmentManager punishmentManager;
    private final ConfigManager configManager;

    public TempBanCommand(PunishmentManager punishmentManager, ConfigManager configManager) {
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /tempban <player> [duration] [reason] [-s]");
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

        List<String> reasonParts = new ArrayList<>();
        String durationStr = null;

        for (int i = 1; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-s")) continue;
            // Check if it's a duration string
            if (durationStr == null && TimeUtil.parseDuration(args[i]) > 0) {
                durationStr = args[i];
            } else {
                reasonParts.add(args[i]);
            }
        }

        if (durationStr == null) {
            durationStr = configManager.getDefaultTempBanDuration();
        }

        long duration = TimeUtil.parseDuration(durationStr);
        long expiration = System.currentTimeMillis() + duration;

        String reason = String.join(" ", reasonParts);
        if (reason.isEmpty()) {
            reason = configManager.getDefaultBanReason();
        }

        String staffName = (sender instanceof Player) ? sender.getName() : "Console";
        String ipAddress = target.isOnline() ? target.getPlayer().getAddress().getAddress().getHostAddress() : null;

        BanEntry ban = new BanEntry(targetUUID, target.getName(), ipAddress, reason, staffName, expiration);
        punishmentManager.addBan(ban);

        String formattedDuration = TimeUtil.formatDuration(duration);

        String kickMessage = configManager.getMessage("kick-messages.tempban")
                .replace("{reason}", reason)
                .replace("{duration}", formattedDuration);

        if (target.isOnline()) {
            target.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', kickMessage));
        }

        String broadcastMessage;
        if (silent) {
            broadcastMessage = configManager.getMessage("silent-option.tempban-broadcast");
        } else {
            broadcastMessage = configManager.getMessage("tempban-broadcast");
        }

        broadcastMessage = broadcastMessage
                .replace("{player}", target.getName())
                .replace("{staff}", staffName)
                .replace("{reason}", reason)
                .replace("{duration}", formattedDuration);

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