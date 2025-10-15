package com.jerae.jsecurity.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jerae.jsecurity.JSecurity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class AuthManager {

    private final JSecurity plugin;
    private final File authFile;
    private final Gson gson;
    private Map<UUID, String> authData = new HashMap<>();
    private final Set<UUID> loggedInPlayers = new HashSet<>();
    private final ConfigManager configManager;

    public AuthManager(JSecurity plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.authFile = new File(plugin.getDataFolder(), "auth.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadAuthData();
    }

    public void loadAuthData() {
        if (authFile.exists()) {
            try (FileReader reader = new FileReader(authFile)) {
                Type type = new TypeToken<Map<UUID, String>>() {}.getType();
                authData = gson.fromJson(reader, type);
                if (authData == null) {
                    authData = new HashMap<>();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not load auth.json");
                e.printStackTrace();
            }
        }
    }

    public void saveAuthData() {
        try (FileWriter writer = new FileWriter(authFile)) {
            gson.toJson(authData, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save auth.json");
            e.printStackTrace();
        }
    }

    public boolean isRegistered(UUID uuid) {
        return authData.containsKey(uuid);
    }

    public void registerPlayer(UUID uuid, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        authData.put(uuid, hashedPassword);
        saveAuthData();
    }

    public boolean checkPassword(UUID uuid, String password) {
        String hashedPassword = authData.get(uuid);
        if (hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(password, hashedPassword);
    }

    public void loginPlayer(Player player) {
        loggedInPlayers.add(player.getUniqueId());
    }

    public void logoutPlayer(Player player) {
        loggedInPlayers.remove(player.getUniqueId());
    }

    public boolean isLoggedIn(Player player) {
        return loggedInPlayers.contains(player.getUniqueId());
    }

    public void unregisterPlayer(UUID uuid) {
        authData.remove(uuid);
        saveAuthData();
    }

    public void changePassword(UUID uuid, String newPassword) {
        registerPlayer(uuid, newPassword);
    }

    public String validatePassword(String password) {
        int minLength = configManager.getMinPasswordLength();
        int maxLength = configManager.getMaxPasswordLength();
        boolean uppercaseRequired = configManager.isUppercaseRequired();
        boolean numberRequired = configManager.isNumberRequired();
        boolean symbolRequired = configManager.isSymbolRequired();

        if (password.length() < minLength) {
            return "Password must be at least " + minLength + " characters long.";
        }

        if (password.length() > maxLength) {
            return "Password cannot be more than " + maxLength + " characters long.";
        }

        StringBuilder requirements = new StringBuilder();
        if (uppercaseRequired && !password.matches(".*[A-Z].*")) {
            requirements.append("an uppercase letter, ");
        }

        if (numberRequired && !password.matches(".*\\d.*")) {
            requirements.append("a number, ");
        }

        if (symbolRequired && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            requirements.append("a symbol, ");
        }

        if (!requirements.isEmpty()) {
            requirements.setLength(requirements.length() - 2);
            return "Password must contain " + requirements.toString() + ".";
        }

        return null;
    }
}