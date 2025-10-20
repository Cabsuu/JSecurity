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

    @Test
    void testGradientWithLegacyBold() {
        String message = "<#FF0000:#00FF00>Hello &lWorld";
        String colorizedMessage = ColorUtil.colorize(message);
        assertEquals("§x§F§F§0§0§0§0H§x§E§A§1§5§1§5e§x§D§5§2§B§2§Al§x§C§0§4§0§4§0l§x§A§B§5§5§5§5o§x§9§6§6§B§6§A §lW§x§6§C§9§5§9§5o§x§5§7§A§B§A§A§lr§x§4§2§C§0§C§0l§x§2§D§D§5§D§5d", colorizedMessage);
    }
}
