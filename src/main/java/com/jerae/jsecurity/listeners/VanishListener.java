package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.managers.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class VanishListener implements Listener {

    private final JSecurity plugin;
    private final VanishManager vanishManager;

    public VanishListener(JSecurity plugin, VanishManager vanishManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (UUID vanishedUUID : vanishManager.getVanishedPlayers()) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedUUID);
            if (vanishedPlayer != null && !player.hasPermission("jsecurity.vanish.see")) {
                player.hidePlayer(plugin, vanishedPlayer);
            }
        }

        if (vanishManager.isVanished(player)) {
            if (player.hasPermission("jsecurity.vanish.silentjoin")) {
                event.setJoinMessage(null);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (vanishManager.isVanished(player)) {
            if (player.hasPermission("jsecurity.vanish.silentquit")) {
                event.setQuitMessage(null);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (vanishManager.isVanished(player) && !player.hasPermission("jsecurity.vanish.attack")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (vanishManager.isVanished(player) && !player.hasPermission("jsecurity.vanish.interact")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            if (vanishManager.isVanished(player)) {
                event.setCancelled(true);
            }
        }
    }
}