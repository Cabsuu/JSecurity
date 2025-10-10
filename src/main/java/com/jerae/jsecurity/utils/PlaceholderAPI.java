package com.jerae.jsecurity.utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PlaceholderAPI {

    private static final Map<String, Function<PlaceholderData, String>> placeholders = new HashMap<>();

    static {
        placeholders.put("{player}", data -> {
            if (data.getTarget() != null) {
                return data.getTarget().getName();
            } else if (data.getPlayerName() != null) {
                return data.getPlayerName();
            }
            return "";
        });
        placeholders.put("{staff}", data -> (data.getStaff() instanceof Player) ? data.getStaff().getName() : "Console");
        placeholders.put("{reason}", data -> data.getReason() != null ? data.getReason() : "");
        placeholders.put("{duration}", data -> data.getDuration() != null ? data.getDuration() : "");
        placeholders.put("{banned_player}", data -> data.getBannedPlayer() != null ? data.getBannedPlayer() : "");
        placeholders.put("{alt_player}", data -> data.getAltPlayer() != null ? data.getAltPlayer() : "");
    }

    public static String setPlaceholders(String message, PlaceholderData data) {
        for (Map.Entry<String, Function<PlaceholderData, String>> entry : placeholders.entrySet()) {
            if (message.contains(entry.getKey())) {
                message = message.replace(entry.getKey(), entry.getValue().apply(data));
            }
        }
        return message;
    }

    public static class PlaceholderData {
        private OfflinePlayer target;
        private String playerName;
        private CommandSender staff;
        private String reason;
        private String duration;
        private String bannedPlayer;
        private String altPlayer;

        public PlaceholderData setTarget(OfflinePlayer target) {
            this.target = target;
            return this;
        }

        public PlaceholderData setPlayerName(String playerName) {
            this.playerName = playerName;
            return this;
        }

        public PlaceholderData setStaff(CommandSender staff) {
            this.staff = staff;
            return this;
        }

        public PlaceholderData setReason(String reason) {
            this.reason = reason;
            return this;
        }

        public PlaceholderData setDuration(String duration) {
            this.duration = duration;
            return this;
        }

        public PlaceholderData setBannedPlayer(String bannedPlayer) {
            this.bannedPlayer = bannedPlayer;
            return this;
        }

        public PlaceholderData setAltPlayer(String altPlayer) {
            this.altPlayer = altPlayer;
            return this;
        }

        public OfflinePlayer getTarget() {
            return target;
        }

        public String getPlayerName() {
            return playerName;
        }

        public CommandSender getStaff() {
            return staff;
        }

        public String getReason() {
            return reason;
        }

        public String getDuration() {
            return duration;
        }

        public String getBannedPlayer() {
            return bannedPlayer;
        }

        public String getAltPlayer() {
            return altPlayer;
        }
    }
}