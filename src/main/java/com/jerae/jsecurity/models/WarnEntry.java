package com.jerae.jsecurity.models;

import java.util.UUID;

public class WarnEntry {
    private final UUID uuid;
    private final String playerName;
    private final String reason;
    private final String staffName;
    private final long timestamp;

    public WarnEntry(UUID uuid, String playerName, String reason, String staffName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.reason = reason;
        this.staffName = staffName;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
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