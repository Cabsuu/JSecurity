package com.jerae.jsecurity.managers;

import com.jerae.jsecurity.JSecurity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final JSecurity plugin;
    private final Set<UUID> vanished = new HashSet<>();

    public VanishManager(JSecurity plugin) {
        this.plugin = plugin;
    }

    public void vanish(Player player) {
        vanished.add(player.getUniqueId());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission("jsecurity.vanish.see")) {
                onlinePlayer.hidePlayer(plugin, player);
            }
        }
    }

    public void unvanish(Player player) {
        vanished.remove(player.getUniqueId());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public Set<UUID> getVanishedPlayers() {
        return vanished;
    }
}