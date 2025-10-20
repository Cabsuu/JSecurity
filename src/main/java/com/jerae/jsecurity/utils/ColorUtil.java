package com.jerae.jsecurity.utils;

import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})>(.+?)");

    public static String colorize(String message) {
        if (message == null) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        while (matcher.find()) {
            String startColor = matcher.group(1);
            String endColor = matcher.group(2);
            String content = matcher.group(3);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(applyGradient(content, startColor, endColor)));
        }
        matcher.appendTail(buffer);
        message = buffer.toString();

        buffer = new StringBuffer();
        matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(ChatColor.of("#" + hexCode).toString()));
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private static String applyGradient(String str, String startHex, String endHex) {
        Color start = hexToColor(startHex);
        Color end = hexToColor(endHex);
        StringBuilder result = new StringBuilder();

        String translatedCodes = ChatColor.translateAlternateColorCodes('&', str);
        String strippedText = ChatColor.stripColor(translatedCodes);
        int textLength = strippedText.length();

        for (int i = 0; i < textLength; i++) {
            double ratio = (textLength <= 1) ? 0.0 : (double) i / (textLength - 1);
            int r = (int) (start.getRed() * (1 - ratio) + end.getRed() * ratio);
            int g = (int) (start.getGreen() * (1 - ratio) + end.getGreen() * ratio);
            int b = (int) (start.getBlue() * (1 - ratio) + end.getBlue() * ratio);

            result.append(ChatColor.of(new Color(r, g, b))).append(strippedText.charAt(i));
        }
        return result.toString();
    }

    private static Color hexToColor(String hex) {
        return new Color(
            Integer.valueOf(hex.substring(0, 2), 16),
            Integer.valueOf(hex.substring(2, 4), 16),
            Integer.valueOf(hex.substring(4, 6), 16)
        );
    }
}
