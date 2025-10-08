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
        if (args.length < 1) {
            Component usageMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&cUsage: /unban <player> [-s]");
            sender.sendMessage(usageMessage);
            return true;
        }

        String targetIdentifier = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetIdentifier);
        UUID targetUUID = target.getUniqueId();

        BanEntry banEntry = punishmentManager.getBan(targetUUID);

        if (banEntry == null) {
            Component notBannedMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&cThat player is not banned.");
            sender.sendMessage(notBannedMessage);
            return true;
        }

        punishmentManager.removeBan(targetUUID);

        String targetName = banEntry.getUsername();

        boolean silent = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-s"));
        if (!silent) {
            String broadcastMessageStr = configManager.getMessage("unban-broadcast")
                    .replace("{player}", targetName)
                    .replace("{staff}", sender.getName());
            Component broadcastMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(broadcastMessageStr);
            Bukkit.getServer().broadcast(broadcastMessage);
        }

        Component successMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&aSuccessfully unbanned " + targetName + ".");
        sender.sendMessage(successMessage);

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