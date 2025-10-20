package com.jerae.jsecurity.utils;

// Make sure to import this one, NOT org.bukkit.ChatColor
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})>(.+?)");

    public static String colorize(String message) {
        if (message == null) {
            return "";
        }

        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        while (matcher.find()) {
            String startColor = matcher.group(1);
            String endColor = matcher.group(2);
            String content = matcher.group(3);
            message = message.replace(matcher.group(), applyGradient(content, startColor, endColor));
        }

        matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            message = message.replace(matcher.group(), ChatColor.of("#" + hexCode).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private static String applyGradient(String str, String startHex, String endHex) {
        StringBuilder sb = new StringBuilder();
        int[] start = hexToRgb(startHex);
        int[] end = hexToRgb(endHex);
        int len = str.length();

        for (int i = 0; i < len; i++) {
            double ratio = (double) i / (len - 1);
            int r = (int) (start[0] * (1 - ratio) + end[0] * ratio);
            int g = (int) (start[1] * (1 - ratio) + end[1] * ratio);
            int b = (int) (start[2] * (1 - ratio) + end[2] * ratio);
            sb.append(ChatColor.of(new java.awt.Color(r, g, b))).append(str.charAt(i));
        }

        return sb.toString();
    }

    private static int[] hexToRgb(String hex) {
        int r = Integer.valueOf(hex.substring(0, 2), 16);
        int g = Integer.valueOf(hex.substring(2, 4), 16);
        int b = Integer.valueOf(hex.substring(4, 6), 16);
        return new int[]{r, g, b};
    }
}
