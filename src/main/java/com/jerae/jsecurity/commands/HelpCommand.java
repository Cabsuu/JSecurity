package com.jerae.jsecurity.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand {

    public static void execute(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- JSecurity Help ---");
        sender.sendMessage(ChatColor.YELLOW + "/js help" + ChatColor.WHITE + " - Shows this help message.");
        sender.sendMessage(ChatColor.YELLOW + "/js reload" + ChatColor.WHITE + " - Reloads the configuration.");
        sender.sendMessage(ChatColor.YELLOW + "/js record [page] [-sort]" + ChatColor.WHITE + " - Shows player records.");
        sender.sendMessage(ChatColor.YELLOW + "/js profile <player>" + ChatColor.WHITE + " - Shows a player's profile.");
        sender.sendMessage(ChatColor.YELLOW + "/js log [page]" + ChatColor.WHITE + " - Shows the punishment log.");
        sender.sendMessage(ChatColor.YELLOW + "/js history <player> [page]" + ChatColor.WHITE + " - Shows a player's punishment history.");
        sender.sendMessage(ChatColor.YELLOW + "/js note <player> <note>" + ChatColor.WHITE + " - Adds a note to a player's profile.");
    }
}