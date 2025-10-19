package com.jerae.jsecurity.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorUtilTest {

    @Test
    void testHexColor() {
        String hexMessage = "&#FF0000Hello";
        Component component = ColorUtil.format(hexMessage);
        assertEquals(TextColor.fromHexString("#FF0000"), component.style().color());
    }

    @Test
    void testGradientColor() {
        String gradientMessage = "<#FF0000>H<#00FF00>e<#0000FF>l<#FFFF00>l<#00FFFF>o";
        Component component = ColorUtil.format(gradientMessage);
        assertEquals(TextColor.fromHexString("#FF0000"), component.style().color());
    }

    @Test
    void testLegacyAndHexColor() {
        String message = "&cHello &#FF0000World";
        Component component = ColorUtil.format(message);
        assertEquals(TextColor.fromHexString("#FF0000"), component.children().get(0).style().color());
    }
}
