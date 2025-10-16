package com.jerae.jsecurity.listeners;

import com.jerae.jsecurity.managers.BanEntry;
import com.jerae.jsecurity.managers.ConfigManager;
import com.jerae.jsecurity.managers.PunishmentManager;
import com.jerae.jsecurity.utils.PlaceholderAPI;
import com.jerae.jsecurity.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.jerae.jsecurity.JSecurity;
import com.jerae.jsecurity.managers.AuthManager;
import com.jerae.jsecurity.managers.InventoryManager;
import com.jerae.jsecurity.managers.PlayerDataManager;
import com.jerae.jsecurity.models.PlayerData;
import com.jerae.jsecurity.commands.LoginCommand;
import com.jerae.jsecurity.commands.RegisterCommand;
import com.jerae.jsecurity.utils.PermissionUtils;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerListener implements Listener {

    private final JSecurity plugin;
    private final PunishmentManager punishmentManager;
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;
    private final AuthManager authManager;
    private final LoginCommand loginCommand;
    private final RegisterCommand registerCommand;
    private final InventoryManager inventoryManager;

    public PlayerListener(JSecurity plugin, PunishmentManager punishmentManager, ConfigManager configManager, PlayerDataManager playerDataManager, AuthManager authManager, InventoryManager inventoryManager) {
        this.plugin = plugin;
        this.punishmentManager = punishmentManager;
        this.configManager = configManager;
        this.playerDataManager = playerDataManager;
        this.authManager = authManager;
        this.loginCommand = new LoginCommand(authManager, configManager);
        this.registerCommand = new RegisterCommand(authManager, configManager);
        this.inventoryManager = inventoryManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String playerName = event.getName();
        String ipAddress = event.getAddress().getHostAddress();

        // Check for existing ban on the UUID
        BanEntry ban = punishmentManager.getBan(uuid);
        if (ban != null) {
            String kickMessage;
            boolean hasReason = ban.getReason() != null && !ban.getReason().isEmpty() && !ban.getReason().equals(configManager.getDefaultBanReason());

            PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                    .setPlayerName(playerName)
                    .setReason(ban.getReason());

            if (ban.isPermanent()) {
                kickMessage = configManager.getMessage("ban-kick-message", hasReason);
            } else {
                data.setDuration(TimeUtil.formatRemainingTime(ban.getExpiration()));
                kickMessage = configManager.getMessage("tempban-kick-message", hasReason);
            }
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(kickMessage, data)));
            return;
        }

        // Check for ban evasion
        if (configManager.isBanEvasionPreventionEnabled()) {
            BanEntry ipBan = punishmentManager.getBanByIp(ipAddress);
            if (ipBan != null) {
                // The player's IP is banned, but their UUID is not. This is ban evasion.
                String originalBannedPlayer = ipBan.getPlayerName();
                PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData().setBannedPlayer(originalBannedPlayer);

                String kickMessage = configManager.getMessage("ban-evasion-kick-message");
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(kickMessage, data)));

                // Create a new ban entry for the evading account
                String evasionReason = configManager.getMessage("ban-evasion-reason");
                BanEntry evasionBan = new BanEntry(
                        uuid,
                        playerName,
                        ipAddress,
                        PlaceholderAPI.setPlaceholders(evasionReason, data),
                        "jSecurity",
                        ipBan.getExpiration() // Match the original ban's expiration
                );
                punishmentManager.addBan(evasionBan);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();

        if (configManager.isAuthEnabled()) {
            authManager.handlePlayerJoin(joiningPlayer);
            if (authManager.isRegistered(joiningPlayer.getUniqueId())) {
                joiningPlayer.sendMessage("Please log in using /login <password>");
            } else {
                joiningPlayer.sendMessage("Please register using /register <password> <confirmPassword>");
            }
        }

        InetSocketAddress joiningPlayerSocketAddress = joiningPlayer.getAddress();

        if (joiningPlayerSocketAddress == null) {
            return;
        }
        String joiningPlayerIp = joiningPlayerSocketAddress.getAddress().getHostAddress();

        if (configManager.isAltAccountAlertEnabled()) {
            Set<String> altAccountNames = new HashSet<>();

            for (PlayerData playerData : playerDataManager.getAllPlayerData()) {
                if (playerData.getIps().contains(joiningPlayerIp)) {
                    altAccountNames.add(playerData.getName());
                }
            }
            // Add the current player to the list if they are not already in it
            // This case can happen if the player data hasn't been saved yet.
            altAccountNames.add(joiningPlayer.getName());

            if (altAccountNames.size() > 1) {
                plugin.getLogger().info("IP Address for " + joiningPlayer.getName() + " is " + joiningPlayerIp);
                plugin.getLogger().info("Found " + altAccountNames.size() + " total accounts on this IP: " + String.join(", ", altAccountNames));

                String otherAccounts = altAccountNames.stream()
                    .filter(name -> !name.equalsIgnoreCase(joiningPlayer.getName()))
                    .collect(Collectors.joining(", "));

                if (otherAccounts.isEmpty()) {
                    return;
                }

                PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                        .setPlayerName(joiningPlayer.getName())
                        .setAltPlayer(otherAccounts);

                String alertMessage = configManager.getMessage("alt-account-alert");
                String formattedMessage = PlaceholderAPI.setPlaceholders(alertMessage, data);

                Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(formattedMessage);

                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("jsecurity.alt.alert"))
                        .forEach(p -> p.sendMessage(component));
            }
        }
    }


    public void onPlayerBan(OfflinePlayer bannedPlayer, String ipAddress) {
        if (configManager.isBanEvasionPreventionEnabled() && ipAddress != null) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.getUniqueId().equals(bannedPlayer.getUniqueId()) && onlinePlayer.getAddress().getAddress().getHostAddress().equals(ipAddress)) {

                    PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                            .setBannedPlayer(bannedPlayer.getName());

                    String kickMessage = configManager.getMessage("kick-messages.alt-account-banned");
                    Component kickComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(PlaceholderAPI.setPlaceholders(kickMessage, data));
                    onlinePlayer.kick(kickComponent);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] parts = event.getMessage().substring(1).split(" ");
        String command = parts[0];
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, parts.length - 1);

        if (configManager.getBoolean("authentication.enabled")) {
            if (command.equalsIgnoreCase("login")) {
                event.setCancelled(true);
                loginCommand.onCommand(player, null, command, args);
                return;
            } else if (command.equalsIgnoreCase("register")) {
                event.setCancelled(true);
                registerCommand.onCommand(player, null, command, args);
                return;
            } else if (!authManager.isLoggedIn(player)) {
                event.setCancelled(true);
                player.sendMessage("You must be logged in to use commands.");
                return;
            }
        }

        if (punishmentManager.isMuted(player.getUniqueId())) {
            if (configManager.getMutedCommandRestriction().contains(command.toLowerCase())) {
                event.setCancelled(true);
                PermissionUtils.sendNoPermissionMessage(player, configManager);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (configManager.isAuthEnabled()) {
            authManager.handlePlayerQuit(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (configManager.isAuthEnabled() && !authManager.isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (configManager.isAuthEnabled() && !authManager.isLoggedIn(player)) {
            event.setCancelled(true);
            player.sendMessage("You must be logged in to chat.");
            return;
        }

        if (punishmentManager.isMuted(player.getUniqueId())) {
            event.setCancelled(true);

            var mute = punishmentManager.getMute(player.getUniqueId());
            String muteMessage;
            PlaceholderAPI.PlaceholderData data = new PlaceholderAPI.PlaceholderData()
                    .setTarget(player)
                    .setReason(mute.getReason());

            if (mute.isPermanent()) {
                muteMessage = configManager.getMessage("mute-message");
            } else {
                data.setDuration(TimeUtil.formatRemainingTime(mute.getExpiration()));
                muteMessage = configManager.getMessage("tempmute-message");
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(muteMessage, data)));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (configManager.isAuthEnabled() && !authManager.isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (configManager.isAuthEnabled() && !authManager.isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (configManager.isAuthEnabled() && !authManager.isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (configManager.isAuthEnabled() && !authManager.isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (configManager.isAuthEnabled() && !authManager.isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (configManager.isAuthEnabled() && !authManager.isLoggedIn(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (configManager.isAuthEnabled() && !authManager.isLoggedIn(player)) {
                event.setCancelled(true);
            }
        }
    }
}