package com.jerae.jsecurity.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryManager {
    private final Map<UUID, ItemStack[]> inventories = new HashMap<>();

    public void saveInventory(Player player) {
        inventories.put(player.getUniqueId(), player.getInventory().getContents());
        player.getInventory().clear();
    }

    public void restoreInventory(Player player) {
        ItemStack[] items = inventories.remove(player.getUniqueId());
        if (items != null) {
            player.getInventory().setContents(items);
        }
    }
}