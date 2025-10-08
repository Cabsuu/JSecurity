package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.managers.ConfigManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            sender.sendMessage(ChatColor.RED + "Usage: /kick <player> [reason] [-s]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        boolean silent = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-s"));
        String reason = Arrays.stream(args)
                .filter(arg -> !arg.equalsIgnoreCase(target.getName()) && !arg.equalsIgnoreCase("-s"))
                .collect(Collectors.joining(" "));

        if (reason.isEmpty()) {
            reason = configManager.getMessage("default-kick-reason");
        }

        String kickMessage = configManager.getMessage("kick-message")
                .replace("{reason}", reason);

        target.kick(LegacyComponentSerializer.legacyAmpersand().deserialize(kickMessage));

        if (!silent) {
            String broadcastMessage = configManager.getMessage("kick-broadcast")
                    .replace("{player}", target.getName())
                    .replace("{staff}", sender.getName())
                    .replace("{reason}", reason);
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMessage));
        }

        sender.sendMessage(ChatColor.GREEN + "Kicked " + target.getName() + ".");
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
        return new ArrayList<>();
    }
}