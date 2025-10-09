package com.jerae.jsecurity.managers;

import java.util.UUID;

public class BanEntry {
    private final UUID uuid;
    private final String playerName;
    private final String ipAddress;
    private final String reason;
    private final String staff;
    private final long expiration; // -1 for permanent

    public BanEntry(UUID uuid, String playerName, String ipAddress, String reason, String staff, long expiration) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.ipAddress = ipAddress;
        this.reason = reason;
        this.staff = staff;
        this.expiration = expiration;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getUsername() {
        return playerName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getReason() {
        return reason;
    }

    public String getStaff() {
        return staff;
    }

    public String getStaffName() {
        return staff;
    }

    public long getExpiration() {
        return expiration;
    }

    public boolean isPermanent() {
        return expiration == -1;
    }

    public boolean hasExpired() {
        if (isPermanent()) {
            return false;
        }
        return System.currentTimeMillis() > expiration;
    }
}