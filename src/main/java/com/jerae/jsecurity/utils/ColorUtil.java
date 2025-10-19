package com.jerae.jsecurity.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component format(String message) {
        return MINI_MESSAGE.deserialize(replaceLegacy(message));
    }

    private static String replaceLegacy(String message) {
        message = message.replace("§", "&");
        message = java.util.regex.Pattern.compile("&#([A-Fa-f0-9]{6})").matcher(message).replaceAll("<#$1>");
        return message;
    }

    public static String toLegacy(Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }
}
