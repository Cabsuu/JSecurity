package com.jerae.jsecurity.commands;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PlayerDataManager;
import com.jerae.jsecurity.managers.AuthManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.models.PlayerData;
import com.jerae.jsecurity.models.PunishmentLogEntry;
import com.jerae.jsecurity.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class JSecurityCommand implements CommandExecutor, TabCompleter {

    private final JSecurity plugin;
    private final ConfigManager configManager;
    private final PunishmentManager punishmentManager;
    private final PlayerDataManager playerDataManager;
    private final AuthManager authManager;
    private final Map<UUID, String> unregisterConfirmation = new HashMap<>();

    public JSecurityCommand(JSecurity plugin, ConfigManager configManager, PunishmentManager punishmentManager, PlayerDataManager playerDataManager, AuthManager authManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.punishmentManager = punishmentManager;
        this.playerDataManager = playerDataManager;
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                if (!sender.hasPermission("jsecurity.help")) {
                    PermissionUtils.sendNoPermissionMessage(sender, configManager);
                    return true;
                }
                HelpCommand.execute(sender, configManager);
                break;
            case "reload":
                if (!sender.hasPermission("jsecurity.admin")) {
                    PermissionUtils.sendNoPermissionMessage(sender, configManager);
                    return true;
                }
                handleReload(sender);
                break;
            case "record":
                if (!sender.hasPermission("jsecurity.record")) {
                    PermissionUtils.sendNoPermissionMessage(sender, configManager);
                    return true;
                }
                handleRecord(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "profile":
                if (!sender.hasPermission("jsecurity.profile")) {
                    PermissionUtils.sendNoPermissionMessage(sender, configManager);
                    return true;
                }
                handleProfile(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "note":
                if (!sender.hasPermission("jsecurity.note")) {
                    PermissionUtils.sendNoPermissionMessage(sender, configManager);
                    return true;
                }
                handleNote(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "log":
                if (!sender.hasPermission("jsecurity.log")) {
                    PermissionUtils.sendNoPermissionMessage(sender, configManager);
                    return true;
                }
                handleLog(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "history":
                if (!sender.hasPermission("jsecurity.history")) {
                    PermissionUtils.sendNoPermissionMessage(sender, configManager);
                    return true;
                }
                handleHistory(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "unregister":
                if (!sender.hasPermission("jsecurity.unregister")) {
                    PermissionUtils.sendNoPermissionMessage(sender, configManager);
                    return true;
                }
                handleUnregister(sender, Arrays.copyOfRange(args, 1, args.length));
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
                sender.sendMessage(configManager.getInvalidPageNumberMessage());
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

        sender.sendMessage(configManager.getJsecurityRecordHeader(page, totalPages));

        int startIndex = (page - 1) * 10;
        int endIndex = Math.min(startIndex + 10, allPlayerData.size());

        for (int i = startIndex; i < endIndex; i++) {
            PlayerData pd = allPlayerData.get(i);
            sender.sendMessage(configManager.getJsecurityRecordFormat(pd.getId(), pd.getName()));
        }
    }

    private void handleProfile(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(configManager.getJsecurityProfileUsageMessage());
            return;
        }

        String playerName = args[0];
        PlayerData playerData = playerDataManager.getPlayerData(playerName);

        if (playerData == null) {
            sender.sendMessage(configManager.getPlayerNotFoundMessage());
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

        sender.sendMessage(configManager.getJsecurityProfileHeader(playerData.getName()));
        sender.sendMessage(configManager.getJsecurityProfileId(playerData.getId()));
        sender.sendMessage(configManager.getJsecurityProfileUuid(playerData.getUuid().toString()));
        sender.sendMessage(configManager.getJsecurityProfileLastIp(playerData.getIps().get(playerData.getIps().size() - 1)));
        sender.sendMessage(configManager.getJsecurityProfileFirstJoined(playerData.getJoined().toString()));
        sender.sendMessage(configManager.getJsecurityProfileStatus(status));

        if (banEntry != null) {
            sender.sendMessage(configManager.getJsecurityProfileBannedReason(banEntry.getReason()));
        }

        if (playerData.getNotes() != null && !playerData.getNotes().isEmpty()) {
            sender.sendMessage(configManager.getJsecurityProfileNotesHeader());
            for (String note : playerData.getNotes()) {
                sender.sendMessage(configManager.getJsecurityProfileNoteFormat(note));
            }
        }
    }

    private void handleLog(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(configManager.getInvalidPageNumberMessage());
                return;
            }
        }

        List<PunishmentLogEntry> logs = punishmentManager.getPunishmentLogs();
        if (logs.isEmpty()) {
            sender.sendMessage(configManager.getJsecurityLogNoLogsMessage());
            return;
        }

        int totalPages = (int) Math.ceil(logs.size() / 10.0);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        sender.sendMessage(configManager.getJsecurityLogHeader(page, totalPages));

        int startIndex = (page - 1) * 10;
        int endIndex = Math.min(startIndex + 10, logs.size());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = startIndex; i < endIndex; i++) {
            PunishmentLogEntry log = logs.get(i);
            LocalDate date = Instant.ofEpochMilli(log.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDate();
            sender.sendMessage(configManager.getJsecurityLogFormat(date.format(formatter), log.getPlayerName(), log.getPunishmentType().toString(), log.getReason()));
        }
    }

    private void handleHistory(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(configManager.getJsecurityHistoryUsageMessage());
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(configManager.getPlayerNotFoundMessage());
            return;
        }

        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(configManager.getInvalidPageNumberMessage());
                return;
            }
        }

        List<PunishmentLogEntry> history = punishmentManager.getPlayerHistory(target.getUniqueId());
        if (history.isEmpty()) {
            sender.sendMessage(configManager.getJsecurityHistoryNoHistoryMessage());
            return;
        }

        int totalPages = (int) Math.ceil(history.size() / 10.0);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        sender.sendMessage(configManager.getJsecurityHistoryHeader(target.getName(), page, totalPages));

        int startIndex = (page - 1) * 10;
        int endIndex = Math.min(startIndex + 10, history.size());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = startIndex; i < endIndex; i++) {
            PunishmentLogEntry log = history.get(i);
            LocalDate date = Instant.ofEpochMilli(log.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDate();
            sender.sendMessage(configManager.getJsecurityHistoryFormat(date.format(formatter), log.getPunishmentType().toString(), log.getReason()));
        }
    }

    private void handleNote(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(configManager.getJsecurityNoteUsageMessage());
            return;
        }

        String playerName = args[0];
        PlayerData playerData = playerDataManager.getPlayerData(playerName);

        if (playerData == null) {
            sender.sendMessage(configManager.getPlayerNotFoundMessage());
            return;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("-clear")) {
            playerData.clearNotes();
            sender.sendMessage(configManager.getJsecurityNoteClearedMessage(playerName));
        } else if (args.length > 1) {
            String note = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            playerData.addNote(note);
            sender.sendMessage(configManager.getJsecurityNoteAddedMessage(playerName));
        } else {
            sender.sendMessage(configManager.getJsecurityNoteUsageMessage());
        }
    }

    private void sendUsage(CommandSender sender) {
        HelpCommand.execute(sender, configManager);
    }

    private void handleUnregister(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(configManager.getJsecurityUnregisterUsageMessage());
            return;
        }

        String playerName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(configManager.getPlayerNotFoundMessage());
            return;
        }

        UUID targetUUID = target.getUniqueId();
        if (!authManager.isRegistered(targetUUID)) {
            sender.sendMessage(configManager.getJsecurityUnregisterNotRegisteredMessage());
            return;
        }

        if (unregisterConfirmation.containsKey(sender instanceof Player ? ((Player) sender).getUniqueId() : null) && unregisterConfirmation.get(sender instanceof Player ? ((Player) sender).getUniqueId() : null).equalsIgnoreCase(playerName)) {
            authManager.unregisterPlayer(targetUUID);
            sender.sendMessage(configManager.getJsecurityUnregisterUnregisteredMessage(playerName));
            System.out.println(playerName + " has been unregistered by " + sender.getName() + ".");
            if (target.isOnline()) {
                authManager.setUnauthenticated(target.getPlayer());
            }
            unregisterConfirmation.remove(sender instanceof Player ? ((Player) sender).getUniqueId() : null);
        } else {
            sender.sendMessage(configManager.getJsecurityUnregisterConfirmMessage(playerName));
            unregisterConfirmation.put(sender instanceof Player ? ((Player) sender).getUniqueId() : null, playerName);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("help", "reload", "record", "profile", "note", "log", "history", "unregister").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("profile") || args[0].equalsIgnoreCase("history") || args[0].equalsIgnoreCase("note")) {
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