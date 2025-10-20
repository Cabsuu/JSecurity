package com.jerae.jsecurity.utils;

import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<(#(?:[A-Fa-f0-9]{6})):(#(?:[A-Fa-f0-9]{6}))>([^<]+)");

    public static String colorize(String message) {
        if (message == null) return "";

        // Apply gradients first
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (gradientMatcher.find()) {
            String startHex = gradientMatcher.group(1);
            String endHex = gradientMatcher.group(2);
            String text = gradientMatcher.group(3);
            String replacement = Matcher.quoteReplacement(applyGradient(text, startHex, endHex));
            gradientMatcher.appendReplacement(buffer, replacement);
        }
        gradientMatcher.appendTail(buffer);
        message = buffer.toString();

        // Then apply hex colors
        Matcher hexMatcher = HEX_PATTERN.matcher(message);
        buffer = new StringBuffer();
        while (hexMatcher.find()) {
            String hexCode = hexMatcher.group(1);
            hexMatcher.appendReplacement(buffer, Matcher.quoteReplacement(ChatColor.of("#" + hexCode).toString()));
        }
        hexMatcher.appendTail(buffer);
        message = buffer.toString();

        // Finally, translate alternate color codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private static String applyGradient(String text, String startHex, String endHex) {
        Color start = Color.decode(startHex);
        Color end = Color.decode(endHex);
        StringBuilder sb = new StringBuilder();

        StringBuilder justTextBuilder = new StringBuilder();
        Map<Integer, String> formatMap = new HashMap<>();
        StringBuilder currentFormat = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if ("klmno".indexOf(code) != -1) {
                    currentFormat.append('&').append(code);
                    i++;
                    continue;
                }
                if (code == 'r' || ("0123456789abcdef".indexOf(code) != -1)) {
                    currentFormat.setLength(0); // Reset formatting
                    i++;
                    continue;
                }
            }
            formatMap.put(justTextBuilder.length(), currentFormat.toString());
            justTextBuilder.append(text.charAt(i));
        }

        String justText = justTextBuilder.toString();
        int textLength = justText.length();

        for (int i = 0; i < textLength; i++) {
            double ratio = (textLength > 1) ? (double) i / (textLength - 1) : 0.5;

            int r = (int) (start.getRed() * (1 - ratio) + end.getRed() * ratio);
            int g = (int) (start.getGreen() * (1 - ratio) + end.getGreen() * ratio);
            int b = (int) (start.getBlue() * (1 - ratio) + end.getBlue() * ratio);

            sb.append(ChatColor.of(new Color(r, g, b)));
            sb.append(formatMap.get(i));
            sb.append(justText.charAt(i));
        }

        return sb.toString();
    }
}
