package com.jerae.jsecurity.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffChatManager {

    private final Set<UUID> staffChatPlayers = new HashSet<>();

    public void toggleStaffChat(UUID playerId) {
        if (staffChatPlayers.contains(playerId)) {
            staffChatPlayers.remove(playerId);
        } else {
            staffChatPlayers.add(playerId);
        }
    }

    public boolean isInStaffChat(UUID playerId) {
        return staffChatPlayers.contains(playerId);
    }
}