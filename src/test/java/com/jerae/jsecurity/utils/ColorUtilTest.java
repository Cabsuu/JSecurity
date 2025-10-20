package com.jerae.jsecurity.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorUtilTest {

    @Test
    void testHexColor() {
        String hexMessage = "&#FF0000Hello";
        String colorizedMessage = ColorUtil.colorize(hexMessage);
        assertEquals("§x§F§F§0§0§0§0Hello", colorizedMessage);
    }

    @Test
    void testGradientColor() {
        String gradientMessage = "<#FF0000:#00FF00>Hello";
        String colorizedMessage = ColorUtil.colorize(gradientMessage);
        assertEquals("§x§F§F§0§0§0§0H§x§C§C§3§3§3§3e§x§9§9§6§6§6§6l§x§6§6§9§9§9§9l§x§3§3§C§C§C§Co", colorizedMessage);
    }

    @Test
    void testLegacyAndHexColor() {
        String message = "&cHello &#FF0000World";
        String colorizedMessage = ColorUtil.colorize(message);
        assertEquals("§cHello §x§F§F§0§0§0§0World", colorizedMessage);
    }
}
