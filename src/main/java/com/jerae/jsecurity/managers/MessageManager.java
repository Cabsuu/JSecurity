package com.jerae.jsecurity.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MessageManager {

    private final Map<UUID, UUID> lastMessage = new HashMap<>();
    private final Set<UUID> socialSpy = new HashSet<>();

    public void sendMessage(Player sender, Player target, String message) {
        sender.sendMessage(ChatColor.GOLD + "To " + target.getName() + ": " + ChatColor.WHITE + message);
        target.sendMessage(ChatColor.GOLD + "From " + sender.getName() + ": " + ChatColor.WHITE + message);

        lastMessage.put(sender.getUniqueId(), target.getUniqueId());
        lastMessage.put(target.getUniqueId(), sender.getUniqueId());

        for (UUID uuid : socialSpy) {
            Player spy = Bukkit.getPlayer(uuid);
            if (spy != null && spy.hasPermission("jsecurity.socialspy.view") && !sender.hasPermission("jsecurity.socialspy.exempt") && !target.hasPermission("jsecurity.socialspy.exempt")) {
                spy.sendMessage(ChatColor.GRAY + "[Spy] " + sender.getName() + " -> " + target.getName() + ": " + message);
            }
        }
    }

    public void replyToMessage(Player sender, String message) {
        UUID targetUUID = lastMessage.get(sender.getUniqueId());
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "You have no one to reply to.");
            return;
        }

        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "The player you were talking to is no longer online.");
            return;
        }

        sendMessage(sender, target, message);
    }

    public boolean toggleSocialSpy(Player player) {
        if (socialSpy.contains(player.getUniqueId())) {
            socialSpy.remove(player.getUniqueId());
            return false;
        } else {
            socialSpy.add(player.getUniqueId());
            return true;
        }
    }
}