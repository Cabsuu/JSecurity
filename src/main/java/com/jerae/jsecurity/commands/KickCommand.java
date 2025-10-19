package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.utils.ColorUtil;
import com.jerae.jsecurity.utils.PermissionUtils;
import com.jerae.jsecurity.utils.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KickCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager configManager;

    public KickCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jsecurity.kick")) {
            PermissionUtils.sendNoPermissionMessage(sender, configManager);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /kick <player> [reason] [-s]"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ColorUtil.colorize("&cPlayer not found."));
            return true;
        }

        boolean silent = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-s"));
        String reason = Arrays.stream(args)
                .skip(1)
                .filter(arg -> !arg.equalsIgnoreCase("-s"))
                .collect(Collectors.joining(" "));

        boolean hasReason = !reason.isEmpty();
        if (!hasReason) {
            reason = configManager.getDefaultKickReason();
        }

        PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                .setTarget(target)
                .setStaff(sender)
                .setReason(reason);

        String kickMessagePath = "kick-message";
        String kickMessageStr = configManager.getMessage(kickMessagePath, hasReason);
        String kickMessage = ColorUtil.colorize(PlaceholderAPI.setPlaceholders(kickMessageStr, data));

        target.kickPlayer(kickMessage);

        if (!silent) {
            String broadcastMessagePath = "kick-broadcast";
            String broadcastMessageStr = configManager.getMessage(broadcastMessagePath, hasReason);
            String broadcastMessage = ColorUtil.colorize(PlaceholderAPI.setPlaceholders(broadcastMessageStr, data));
            Bukkit.getServer().broadcastMessage(broadcastMessage);
        }

        sender.sendMessage(ColorUtil.colorize(PlaceholderAPI.setPlaceholders(configManager.getMessage("kick-success", true), data)));
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
                return new ArrayList<>(List.of("-s"));
            }
        }
        return new ArrayList<>();
    }
}