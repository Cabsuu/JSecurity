package com.jerae.jsecurity.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MessageManager {

    private final ConfigManager configManager;
    private final Map<UUID, UUID> lastMessage = new HashMap<>();
    private final Set<UUID> socialSpy = new HashSet<>();

    public MessageManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void sendMessage(Player sender, Player target, String message) {
        String toSender = configManager.getPrivateMessageToSenderFormat()
                .replace("{target}", target.getName())
                .replace("{content}", message);
        String toReceiver = configManager.getPrivateMessageToReceiverFormat()
                .replace("{sender}", sender.getName())
                .replace("{content}", message);

        sender.sendMessage(com.jerae.jsecurity.utils.ColorUtil.colorize(toSender));
        target.sendMessage(com.jerae.jsecurity.utils.ColorUtil.colorize(toReceiver));

        lastMessage.put(sender.getUniqueId(), target.getUniqueId());
        lastMessage.put(target.getUniqueId(), sender.getUniqueId());

        for (UUID uuid : socialSpy) {
            Player spy = Bukkit.getPlayer(uuid);
            if (spy != null && spy.hasPermission("jsecurity.socialspy.view") && !sender.hasPermission("jsecurity.socialspy.exempt") && !target.hasPermission("jsecurity.socialspy.exempt")) {
                spy.sendMessage(com.jerae.jsecurity.utils.ColorUtil.colorize("&7[Spy] " + sender.getName() + " -> " + target.getName() + ": " + message));
            }
        }
    }

    public void replyToMessage(Player sender, String message) {
        UUID targetUUID = lastMessage.get(sender.getUniqueId());
        if (targetUUID == null) {
            sender.sendMessage(com.jerae.jsecurity.utils.ColorUtil.colorize("&cYou have no one to reply to."));
            return;
        }

        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null) {
            sender.sendMessage(com.jerae.jsecurity.utils.ColorUtil.colorize("&cThe player you were talking to is no longer online."));
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