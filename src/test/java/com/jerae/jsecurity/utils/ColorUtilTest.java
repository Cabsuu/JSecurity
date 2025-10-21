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
        assertEquals("§x§f§f§0§0§0§0H§x§b§f§3§f§0§0e§x§7§f§7§f§0§0l§x§3§f§b§f§0§0l§x§0§0§f§f§0§0o", colorizedMessage);
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
        assertEquals("§x§f§f§0§0§0§0H§x§e§5§1§9§0§0e§x§c§c§3§3§0§0l§x§b§2§4§c§0§0l§x§9§9§6§6§0§0o§x§7§f§7§f§0§0 §x§6§6§9§9§0§0§lW§x§4§c§b§2§0§0§lo§x§3§2§c§c§0§0§lr§x§1§9§e§5§0§0§ll§x§0§0§f§f§0§0§ld", colorizedMessage);
    }

    @Test
    public void testColorize() {
        String message = "&aHello, &cWorld!";
        String expected = "§aHello, §cWorld!";
        assertEquals(expected, ColorUtil.colorize(message));
    }
}
