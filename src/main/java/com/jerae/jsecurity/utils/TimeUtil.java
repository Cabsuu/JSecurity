package com.jerae.jsecurity.utils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([smhdy])");

    /**
     * Parses a duration string like "1d12h30m" into milliseconds.
     *
     * @param durationStr The string to parse.
     * @return The duration in milliseconds, or 0 if the string is invalid.
     */
    public static long parseDuration(String durationStr) {
        long totalSeconds = 0;
        Matcher matcher = DURATION_PATTERN.matcher(durationStr.toLowerCase());
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            char unit = matcher.group(2).charAt(0);
            switch (unit) {
                case 's':
                    totalSeconds += value;
                    break;
                case 'm':
                    totalSeconds += value * 60;
                    break;
                case 'h':
                    totalSeconds += value * 60 * 60;
                    break;
                case 'd':
                    totalSeconds += value * 24 * 60 * 60;
                    break;
                case 'y': // Not standard, but useful
                    totalSeconds += value * 365 * 24 * 60 * 60;
                    break;
            }
        }
        return totalSeconds * 1000;
    }

    /**
     * Formats a duration in milliseconds into a human-readable string.
     *
     * @param millis The duration in milliseconds.
     * @return A formatted string (e.g., "1 day, 2 hours, 3 minutes, 4 seconds").
     */
    public static String formatDuration(long millis) {
        if (millis < 0) {
            return "Permanent";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        if (days >= 365) {
            long years = days / 365;
            return years + " year" + (years > 1 ? "s" : "");
        }
        if (days >= 30) {
            long months = days / 30;
            return months + " month" + (months > 1 ? "s" : "");
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" day").append(days > 1 ? "s" : "").append(", ");
        }
        if (hours > 0) {
            sb.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(", ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(", ");
        }
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append(" second").append(seconds != 1 ? "s" : "");
        }

        // Remove trailing ", " if it exists
        if (sb.length() > 2 && sb.substring(sb.length() - 2).equals(", ")) {
            return sb.substring(0, sb.length() - 2);
        }

        return sb.toString();
    }

    public static String formatRemainingTime(long expiration) {
        if (expiration == -1) {
            return "Permanent";
        }
        long remainingMillis = expiration - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return "Expired";
        }
        return formatDuration(remainingMillis);
    }
}