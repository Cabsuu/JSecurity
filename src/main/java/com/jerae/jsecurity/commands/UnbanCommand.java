package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.utils.ColorUtil;
import com.jerae.jsecurity.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
        if (!sender.hasPermission("jsecurity.unban")) {
            PermissionUtils.sendNoPermissionMessage(sender, configManager);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /unban <player> [-s]"));
            return true;
        }

        String targetIdentifier = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetIdentifier);
        UUID targetUUID = target.getUniqueId();

        BanEntry banEntry = punishmentManager.getBan(targetUUID);

        if (banEntry == null) {
            sender.sendMessage(ColorUtil.colorize("&cThat player is not banned."));
            return true;
        }

        punishmentManager.removeBan(targetUUID);

        String targetName = banEntry.getUsername();

        boolean silent = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-s"));
        if (!silent) {
            String broadcastMessageStr = configManager.getMessage("unban-broadcast")
                    .replace("{player}", targetName)
                    .replace("{staff}", sender.getName());
            String broadcastMessage = ColorUtil.colorize(broadcastMessageStr);
            Bukkit.getServer().broadcastMessage(broadcastMessage);
        }

        sender.sendMessage(ColorUtil.colorize("&aSuccessfully unbanned " + targetName + "."));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return punishmentManager.getBannedPlayers().stream()
                    .map(BanEntry::getUsername)
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