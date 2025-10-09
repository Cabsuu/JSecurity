package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PlayerDataManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.models.PlayerData;
import com.jerae.jsecurity.models.PunishmentLogEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JSecurityCommand implements CommandExecutor, TabCompleter {

    private final JSecurity plugin;
    private final ConfigManager configManager;
    private final PunishmentManager punishmentManager;
    private final PlayerDataManager playerDataManager;

    public JSecurityCommand(JSecurity plugin, ConfigManager configManager, PunishmentManager punishmentManager, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.punishmentManager = punishmentManager;
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "record":
                handleRecord(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "player":
                handlePlayer(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "log":
                handleLog(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "history":
                handleHistory(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        configManager.reloadConfig();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', configManager.getReloadMessage()));
    }

    private void handleRecord(CommandSender sender, String[] args) {
        int page = 1;
        boolean sort = false;
        List<String> argsList = new ArrayList<>(Arrays.asList(args));

        if (argsList.contains("-sort")) {
            sort = true;
            argsList.remove("-sort");
        }

        if (!argsList.isEmpty()) {
            try {
                page = Integer.parseInt(argsList.get(0));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid page number.");
                return;
            }
        }

        List<PlayerData> allPlayerData = new ArrayList<>(playerDataManager.getAllPlayerData());

        if (sort) {
            allPlayerData.sort(Comparator.comparing(PlayerData::getName, String.CASE_INSENSITIVE_ORDER));
        } else {
            allPlayerData.sort(Comparator.comparingInt(PlayerData::getId));
        }

        int totalPages = (int) Math.ceil(allPlayerData.size() / 10.0);
        if (page > totalPages || page < 1) {
            page = 1;
        }

        sender.sendMessage(ChatColor.GOLD + "--- Player Records (Page " + page + "/" + totalPages + ") ---");

        int startIndex = (page - 1) * 10;
        int endIndex = Math.min(startIndex + 10, allPlayerData.size());

        for (int i = startIndex; i < endIndex; i++) {
            PlayerData pd = allPlayerData.get(i);
            sender.sendMessage(ChatColor.YELLOW + "" + pd.getId() + ". " + ChatColor.WHITE + pd.getName());
        }
    }

    private void handlePlayer(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /js player <player>");
            return;
        }

        String playerName = args[0];
        PlayerData playerData = playerDataManager.getPlayerData(playerName);

        if (playerData == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerData.getUuid());
        BanEntry banEntry = punishmentManager.getBan(offlinePlayer.getUniqueId());

        String status;
        if (banEntry != null) {
            status = ChatColor.RED + "Banned";
        } else if (offlinePlayer.isOnline()) {
            status = ChatColor.GREEN + "Online";
        } else {
            status = ChatColor.GRAY + "Offline";
        }

        sender.sendMessage(ChatColor.GOLD + "--- Player Profile: " + playerData.getName() + " ---");
        sender.sendMessage(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + playerData.getId());
        sender.sendMessage(ChatColor.YELLOW + "UUID: " + ChatColor.WHITE + playerData.getUuid());
        sender.sendMessage(ChatColor.YELLOW + "Last IP: " + ChatColor.WHITE + playerData.getIps().get(playerData.getIps().size() - 1));
        sender.sendMessage(ChatColor.YELLOW + "First Joined: " + ChatColor.WHITE + playerData.getJoined());
        sender.sendMessage(ChatColor.YELLOW + "Status: " + status);

        if (banEntry != null) {
            sender.sendMessage(ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + banEntry.getReason());
        }
    }

    private void handleLog(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid page number.");
                return;
            }
        }

        List<PunishmentLogEntry> logs = punishmentManager.getPunishmentLogs();
        if (logs.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "There are no punishment logs.");
            return;
        }

        int totalPages = (int) Math.ceil(logs.size() / 10.0);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        sender.sendMessage(ChatColor.GOLD + "--- Punishment Log (Page " + page + "/" + totalPages + ") ---");

        int startIndex = (page - 1) * 10;
        int endIndex = Math.min(startIndex + 10, logs.size());

        for (int i = startIndex; i < endIndex; i++) {
            PunishmentLogEntry log = logs.get(i);
            sender.sendMessage(ChatColor.YELLOW + log.getPlayerName() + " - " + log.getPunishmentType() + " - " + log.getReason());
        }
    }

    private void handleHistory(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /js history <player> [page]");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid page number.");
                return;
            }
        }

        List<PunishmentLogEntry> history = punishmentManager.getPlayerHistory(target.getUniqueId());
        if (history.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "This player has no punishment history.");
            return;
        }

        int totalPages = (int) Math.ceil(history.size() / 10.0);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        sender.sendMessage(ChatColor.GOLD + "--- History for " + target.getName() + " (Page " + page + "/" + totalPages + ") ---");

        int startIndex = (page - 1) * 10;
        int endIndex = Math.min(startIndex + 10, history.size());

        for (int i = startIndex; i < endIndex; i++) {
            PunishmentLogEntry log = history.get(i);
            sender.sendMessage(ChatColor.YELLOW + log.getPunishmentType() + " - " + log.getReason());
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Usage: /js <reload|record|player|log|history>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "record", "player", "log", "history").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("history")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("record")) {
                if (!args[1].equalsIgnoreCase("-sort")) {
                    return Collections.singletonList("-sort");
                }
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("record")) {
            if (!args[1].equalsIgnoreCase("-sort") && !args[2].equalsIgnoreCase("-sort")) {
                return Collections.singletonList("-sort");
            }
        }

        return Collections.emptyList();
    }
}