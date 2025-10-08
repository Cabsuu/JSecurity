package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class JSecurityCommand implements CommandExecutor, TabCompleter {

    private final JSecurity plugin;
    private final ConfigManager configManager;

    public JSecurityCommand(JSecurity plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            configManager.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "jSecurity configuration reloaded.");
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Usage: /js reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}