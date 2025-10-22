package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.InvseeManager;
import com.jerae.jsecurity.utils.PlaceholderUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InvseeListener implements Listener {

    private final InvseeManager invseeManager;
    private final ConfigManager configManager;

    public InvseeListener(InvseeManager invseeManager, ConfigManager configManager) {
        this.invseeManager = invseeManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player staff = (Player) event.getWhoClicked();

        if (event.getInventory().getHolder() instanceof InvseeManager.InvseeHolder) {
            event.setCancelled(true);
            Player target = invseeManager.getTarget(staff);
            if (target == null) {
                return;
            }

            switch (event.getSlot()) {
                case 8: // Close
                    staff.closeInventory();
                    break;
                case 47: // Clear
                    if (staff.hasPermission("jsecurity.invsee.clear")) {
                        target.getInventory().clear();
                        staff.sendMessage(PlaceholderUtil.setPlaceholders(target, configManager.getInventoryClearedMessage()));
                    } else {
                        staff.sendMessage(configManager.getNoPermissionMessage());
                    }
                    break;
                case 49: // Ender Chest
                    if (staff.hasPermission("jsecurity.invsee.enderchest")) {
                        invseeManager.openEnderChest(staff, target);
                    } else {
                        staff.sendMessage(configManager.getNoPermissionMessage());
                    }
                    break;
                case 51: // Teleport
                    if (staff.hasPermission("jsecurity.invsee.teleport")) {
                        staff.teleport(target.getLocation());
                        staff.sendMessage(PlaceholderUtil.setPlaceholders(target, configManager.getTeleportSuccessMessage()));
                    } else {
                        staff.sendMessage(configManager.getNoPermissionMessage());
                    }
                    break;
            }
        } else if (event.getInventory().getHolder() instanceof InvseeManager.EnderchestHolder) {
            event.setCancelled(true);
            Player target = invseeManager.getTarget(staff);
            if (target == null) {
                return;
            }

            if (event.getSlot() == 35) { // Back
                invseeManager.openInventory(staff, target);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player staff = (Player) event.getPlayer();
        if (event.getInventory().getHolder() instanceof InvseeManager.InvseeHolder || event.getInventory().getHolder() instanceof InvseeManager.EnderchestHolder) {
            invseeManager.closeInventory(staff);
        }
    }
}
