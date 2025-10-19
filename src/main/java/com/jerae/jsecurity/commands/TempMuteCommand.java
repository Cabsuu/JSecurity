package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.MuteEntry;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.utils.PermissionUtils;
import com.jerae.jsecurity.utils.ColorUtil;
import com.jerae.jsecurity.utils.PlaceholderAPI;
import com.jerae.jsecurity.utils.TimeUtil;
import net.kyori.adventure.text.Component;
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

public class TempMuteCommand implements CommandExecutor, TabCompleter {

    private final PunishmentManager punishmentManager;
    private final ConfigManager configManager;

    public TempMuteCommand(PunishmentManager punishmentManager, ConfigManager configManager) {
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jsecurity.tempmute")) {
            PermissionUtils.sendNoPermissionMessage(sender, configManager);
            return true;
        }

        if (args.length < 2) {
            Component usageMessage = ColorUtil.format("&cUsage: /tempmute <player> <duration> [reason] [-s]");
            sender.sendMessage(usageMessage);
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            Component playerNotFoundMessage = ColorUtil.format("&cPlayer not found.");
            sender.sendMessage(playerNotFoundMessage);
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        if (punishmentManager.isMuted(targetUUID)) {
            Component alreadyMutedMessage = ColorUtil.format("&cThat player is already muted.");
            sender.sendMessage(alreadyMutedMessage);
            return true;
        }

        String durationStr = args[1];
        long duration = TimeUtil.parseDuration(durationStr);
        if (duration <= 0) {
            durationStr = configManager.getDefaultTempMuteDuration();
            duration = TimeUtil.parseDuration(durationStr);
        }
        long expiration = System.currentTimeMillis() + duration;

        boolean silent = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-s"));
        String reason = Arrays.stream(args)
                .skip(2)
                .filter(arg -> !arg.equalsIgnoreCase("-s"))
                .collect(Collectors.joining(" "));

        boolean hasReason = !reason.isEmpty();
        if (!hasReason) {
            reason = configManager.getDefaultMuteReason();
        }

        String staffName = (sender instanceof Player) ? sender.getName() : "Console";

        MuteEntry mute = new MuteEntry(targetUUID, target.getName(), reason, staffName, expiration);
        punishmentManager.addMute(mute);

        String formattedDuration = TimeUtil.formatDuration(duration);

        PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                .setTarget(target)
                .setStaff(sender)
                .setReason(reason)
                .setDuration(formattedDuration);

        String muteMessagePath = "tempmute-message";
        String muteMessageStr = configManager.getMessage(muteMessagePath, hasReason);
        Component muteMessage = ColorUtil.format(PlaceholderAPI.setPlaceholders(muteMessageStr, data));

        if (target.isOnline()) {
            target.getPlayer().sendMessage(muteMessage);
        }

        if (!silent) {
            String broadcastMessagePath = "tempmute-broadcast";
            String broadcastMessageStr = configManager.getMessage(broadcastMessagePath, hasReason);
            Component broadcastMessage = ColorUtil.format(PlaceholderAPI.setPlaceholders(broadcastMessageStr, data));
            Bukkit.getServer().broadcast(broadcastMessage);
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
        if (args.length > 2) {
            if ("-s".startsWith(args[args.length - 1].toLowerCase())) {
                return new ArrayList<>(List.of("-s"));
            }
        }
        return new ArrayList<>();
    }
}