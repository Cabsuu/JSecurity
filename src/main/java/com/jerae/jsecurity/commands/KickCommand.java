package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
        if (args.length < 1) {
            Component usageMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&cUsage: /kick <player> [reason] [-s]");
            sender.sendMessage(usageMessage);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Component playerNotFoundMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&cPlayer not found.");
            sender.sendMessage(playerNotFoundMessage);
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

        String kickMessagePath = "kick-message";
        String kickMessageStr = configManager.getMessage(kickMessagePath, hasReason)
                .replace("{reason}", reason);
        Component kickMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(kickMessageStr);

        target.kick(kickMessage);

        if (!silent) {
            String broadcastMessagePath = "kick-broadcast";
            String broadcastMessageStr = configManager.getMessage(broadcastMessagePath, hasReason)
                    .replace("{player}", target.getName())
                    .replace("{staff}", sender.getName())
                    .replace("{reason}", reason);
            Component broadcastMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(broadcastMessageStr);
            Bukkit.getServer().broadcast(broadcastMessage);
        }

        Component successMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&aKicked " + target.getName() + ".");
        sender.sendMessage(successMessage);
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