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
        String gradientMessage = "<#FF0000>Hello<#00FF00>";
        String colorizedMessage = ColorUtil.colorize(gradientMessage);
        assert(!colorizedMessage.equals("<#FF0000>Hello<#00FF00>"));
    }

    @Test
    void testLegacyAndHexColor() {
        String message = "&cHello &#FF0000World";
        String colorizedMessage = ColorUtil.colorize(message);
        assertEquals("§cHello §x§F§F§0§0§0§0World", colorizedMessage);
    }
}
