package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.listeners.PlayerListener;
import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PlayerDataManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.models.PlayerData;
import com.jerae.jsecurity.utils.PermissionUtils;
import com.jerae.jsecurity.utils.ColorUtil;
import com.jerae.jsecurity.utils.PlaceholderAPI;
import org.bukkit.Bukkit;
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
    private final PlayerListener playerListener;
    private final PlayerDataManager playerDataManager;

    public BanCommand(PunishmentManager punishmentManager, ConfigManager configManager, PlayerListener playerListener, PlayerDataManager playerDataManager) {
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
        this.playerListener = playerListener;
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jsecurity.ban")) {
            PermissionUtils.sendNoPermissionMessage(sender, configManager);
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /ban <player> [reason] [-s]"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ColorUtil.colorize("&cPlayer not found."));
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        if (punishmentManager.isBanned(targetUUID)) {
            sender.sendMessage(ColorUtil.colorize("&cThat player is already banned."));
            return true;
        }

        boolean silent = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-s"));
        String reason = Arrays.stream(args)
                .skip(1)
                .filter(arg -> !arg.equalsIgnoreCase("-s"))
                .collect(Collectors.joining(" "));

        boolean hasReason = !reason.isEmpty();
        if (!hasReason) {
            reason = configManager.getDefaultBanReason();
        }

        String staffName = (sender instanceof Player) ? sender.getName() : "Console";
        String ipAddress = null;
        if (target.isOnline()) {
            ipAddress = target.getPlayer().getAddress().getAddress().getHostAddress();
        } else {
            PlayerData playerData = playerDataManager.getPlayerData(target.getUniqueId());
            if (playerData != null && !playerData.getIps().isEmpty()) {
                ipAddress = playerData.getIps().get(playerData.getIps().size() - 1);
            }
        }

        BanEntry ban = new BanEntry(targetUUID, target.getName(), ipAddress, reason, staffName, -1);
        punishmentManager.addBan(ban);

        PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                .setTarget(target)
                .setStaff(sender)
                .setReason(reason);

        String kickMessagePath = "ban-kick-message";
        String kickMessageStr = configManager.getMessage(kickMessagePath, hasReason);
        String kickMessage = ColorUtil.colorize(PlaceholderAPI.setPlaceholders(kickMessageStr, data));

        if (target.isOnline()) {
            target.getPlayer().kickPlayer(kickMessage);
        }

        playerListener.onPlayerBan(target, ipAddress);

        if (!silent) {
            String broadcastMessagePath = "ban-broadcast";
            String broadcastMessageStr = configManager.getMessage(broadcastMessagePath, hasReason);
            String broadcastMessage = ColorUtil.colorize(PlaceholderAPI.setPlaceholders(broadcastMessageStr, data));
            Bukkit.getServer().broadcastMessage(broadcastMessage);
        }

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