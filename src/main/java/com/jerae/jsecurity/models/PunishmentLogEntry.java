package com.jerae.jsecurity.models;

import java.util.UUID;

public class PunishmentLogEntry {
    private final String playerName;
    private final UUID playerUUID;
    private final String punishmentType;
    private final String reason;
    private final String staffName;
    private final long timestamp;

    public PunishmentLogEntry(String playerName, UUID playerUUID, String punishmentType, String reason, String staffName, long timestamp) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.punishmentType = punishmentType;
        this.reason = reason;
        this.staffName = staffName;
        this.timestamp = timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPunishmentType() {
        return punishmentType;
    }

    public String getReason() {
        return reason;
    }

    public String getStaffName() {
        return staffName;
    }

    public long getTimestamp() {
        return timestamp;
    }
}