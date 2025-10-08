package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.FreezeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerFreezeListener implements Listener {

    private final FreezeManager freezeManager;

    public PlayerFreezeListener(FreezeManager freezeManager) {
        this.freezeManager = freezeManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (freezeManager.isFrozen(event.getPlayer())) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (freezeManager.isFrozen(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (freezeManager.isFrozen(damager)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (freezeManager.isFrozen(event.getPlayer())) {
            // Optional: Punish player for leaving while frozen
            // For now, we just unfreeze them to prevent issues.
            freezeManager.unfreeze(event.getPlayer());
        }
    }
}