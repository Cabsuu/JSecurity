package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    public BanCommand(PunishmentManager punishmentManager, ConfigManager configManager) {
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            Component usageMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&cUsage: /ban <player> [reason] [-s]");
            sender.sendMessage(usageMessage);
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            Component playerNotFoundMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&cPlayer not found.");
            sender.sendMessage(playerNotFoundMessage);
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        if (punishmentManager.isBanned(targetUUID)) {
            Component alreadyBannedMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&cThat player is already banned.");
            sender.sendMessage(alreadyBannedMessage);
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
        String ipAddress = target.isOnline() ? target.getPlayer().getAddress().getAddress().getHostAddress() : null;

        BanEntry ban = new BanEntry(targetUUID, target.getName(), ipAddress, reason, staffName, -1);
        punishmentManager.addBan(ban);

        String kickMessagePath = "ban-kick-message";
        String kickMessageStr = configManager.getMessage(kickMessagePath, hasReason)
                .replace("{reason}", reason);
        Component kickMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(kickMessageStr);

        if (target.isOnline()) {
            target.getPlayer().kick(kickMessage);
        }

        if (!silent) {
            String broadcastMessagePath = "ban-broadcast";
            String broadcastMessageStr = configManager.getMessage(broadcastMessagePath, hasReason)
                    .replace("{player}", target.getName())
                    .replace("{staff}", staffName)
                    .replace("{reason}", reason);
            Component broadcastMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(broadcastMessageStr);
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
        if (args.length > 1) {
            if ("-s".startsWith(args[args.length - 1].toLowerCase())) {
                return new ArrayList<>(List.of("-s"));
            }
        }
        return new ArrayList<>();
    }
}