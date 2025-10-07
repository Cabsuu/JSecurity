package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.MuteEntry;
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

public class TempMuteCommand implements CommandExecutor, TabCompleter {

    private final PunishmentManager punishmentManager;
    private final ConfigManager configManager;

    public TempMuteCommand(PunishmentManager punishmentManager, ConfigManager configManager) {
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /tempmute <player> [duration] [reason] [-s]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || !target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        if (punishmentManager.isMuted(targetUUID)) {
            sender.sendMessage(ChatColor.RED + "That player is already muted.");
            return true;
        }

        boolean silent = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-s"));

        List<String> reasonParts = new ArrayList<>();
        String durationStr = null;

        for (int i = 1; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-s")) continue;
            if (durationStr == null && TimeUtil.parseDuration(args[i]) > 0) {
                durationStr = args[i];
            } else {
                reasonParts.add(args[i]);
            }
        }

        if (durationStr == null) {
            durationStr = configManager.getDefaultTempMuteDuration();
        }

        long duration = TimeUtil.parseDuration(durationStr);
        long expiration = System.currentTimeMillis() + duration;

        String reason = String.join(" ", reasonParts);
        if (reason.isEmpty()) {
            reason = configManager.getDefaultMuteReason();
        }

        String staffName = (sender instanceof Player) ? sender.getName() : "Console";

        MuteEntry mute = new MuteEntry(targetUUID, target.getName(), reason, staffName, expiration);
        punishmentManager.addMute(mute);

        String formattedDuration = TimeUtil.formatDuration(duration);

        String muteMessage = configManager.getMessage("tempmute-message")
                .replace("{reason}", reason)
                .replace("{duration}", formattedDuration);

        if (target.isOnline()) {
            target.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', muteMessage));
        }

        String broadcastMessage;
        if (silent) {
            broadcastMessage = configManager.getMessage("silent-option.tempmute-broadcast");
        } else {
            broadcastMessage = configManager.getMessage("tempmute-broadcast");
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