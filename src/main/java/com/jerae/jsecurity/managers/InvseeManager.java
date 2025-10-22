package com.jerae.jsecurity.managers;

import com.jerae.jsecurity.utils.ColorUtil;
import com.jerae.jsecurity.utils.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class InvseeManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<Player, BukkitRunnable> viewingTasks = new HashMap<>();
    private final Map<Player, Player> viewingMap = new HashMap<>();

    public InvseeManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void openInventory(Player staff, Player target) {
        String title = configManager.getInvseeTitle();
        Inventory inv = Bukkit.createInventory(new InvseeHolder(), 54, ColorUtil.colorize(PlaceholderUtil.setPlaceholders(target, title)));

        staff.openInventory(inv);
        viewingMap.put(staff, target);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!staff.isOnline() || !staff.getOpenInventory().getTopInventory().equals(inv)) {
                    cancel();
                    return;
                }
                updateInventory(staff, target, inv);
            }
        };

        task.runTaskTimer(plugin, 0L, 20L);
        viewingTasks.put(staff, task);
    }

    private void updateInventory(Player staff, Player target, Inventory inv) {
        inv.clear();

        // Row 1: Player Head, Armor, Offhand, Close Button
        inv.setItem(0, getPlayerHead(target));
        inv.setItem(1, target.getInventory().getHelmet());
        inv.setItem(2, target.getInventory().getChestplate());
        inv.setItem(3, target.getInventory().getLeggings());
        inv.setItem(4, target.getInventory().getBoots());
        inv.setItem(6, target.getInventory().getItemInOffHand());
        inv.setItem(8, createButton(Material.BARRIER, configManager.getCloseButtonName()));

        // Rows 2-4: Main Inventory (slots 9-35)
        for (int i = 9; i <= 35; i++) {
            inv.setItem(i, target.getInventory().getItem(i));
        }

        // Row 5: Hotbar (slots 0-8)
        for (int i = 0; i <= 8; i++) {
            inv.setItem(i + 36, target.getInventory().getItem(i));
        }

        // Row 6: Action Buttons
        inv.setItem(47, createButton(Material.TNT, configManager.getClearButtonName()));
        inv.setItem(49, createButton(Material.ENDER_CHEST, configManager.getEnderChestButtonName()));
        inv.setItem(51, createButton(Material.ENDER_PEARL, configManager.getTeleportButtonName()));


        // Fillers
        ItemStack filler = createFiller();
        inv.setItem(5, filler);
        inv.setItem(7, filler);
        inv.setItem(45, filler);
        inv.setItem(46, filler);
        inv.setItem(48, filler);
        inv.setItem(50, filler);
        inv.setItem(52, filler);
        inv.setItem(53, filler);
    }

    public void openEnderChest(Player staff, Player target) {
        String title = configManager.getEnderChestTitle();
        Inventory inv = Bukkit.createInventory(new EnderchestHolder(), 36, ColorUtil.colorize(PlaceholderUtil.setPlaceholders(target, title)));

        // Rows 1-3: Ender Chest Contents
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, target.getEnderChest().getItem(i));
        }

        // Row 4: Back Button
        inv.setItem(35, createButton(Material.ARROW, configManager.getBackButtonName()));

        // Fillers
        ItemStack filler = createFiller();
        for (int i = 27; i < 35; i++) {
            inv.setItem(i, filler);
        }

        staff.openInventory(inv);

        // Explicitly set fillers for the 4th row
        for (int i = 27; i < 35; i++) {
            inv.setItem(i, filler);
        }
    }


    public void closeInventory(Player staff) {
        if (viewingTasks.containsKey(staff)) {
            viewingTasks.get(staff).cancel();
            viewingTasks.remove(staff);
        }
        viewingMap.remove(staff);
    }

    public Player getTarget(Player staff) {
        return viewingMap.get(staff);
    }

    private ItemStack getPlayerHead(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(ColorUtil.colorize("&f" + player.getName()));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createButton(Material material, String name) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static class InvseeHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }

    public static class EnderchestHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
