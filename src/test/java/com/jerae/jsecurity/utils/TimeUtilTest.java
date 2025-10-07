package com.jerae.jsecurity.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeUtilTest {

    @Test
    void testParseDuration() {
        assertEquals(1000, TimeUtil.parseDuration("1s"));
        assertEquals(60 * 1000, TimeUtil.parseDuration("1m"));
        assertEquals(60 * 60 * 1000, TimeUtil.parseDuration("1h"));
        assertEquals(24 * 60 * 60 * 1000, TimeUtil.parseDuration("1d"));
        assertEquals(365 * 24 * 60 * 60 * 1000L, TimeUtil.parseDuration("1y"));

        // Test combination
        long expected = (1 * 24 * 60 * 60 * 1000L) + (12 * 60 * 60 * 1000L);
        assertEquals(expected, TimeUtil.parseDuration("1d12h"));

        // Test with spaces (should also work with current regex)
        assertEquals(expected, TimeUtil.parseDuration("1d 12h"));

        // Test invalid format
        assertEquals(0, TimeUtil.parseDuration("1w")); // 'w' for week is not supported
        assertEquals(0, TimeUtil.parseDuration("abc"));
    }

    @Test
    void testFormatDuration() {
        assertEquals("Permanent", TimeUtil.formatDuration(-1));
        assertEquals("0 seconds", TimeUtil.formatDuration(0));
        assertEquals("1 second", TimeUtil.formatDuration(1000));
        assertEquals("1 minute", TimeUtil.formatDuration(60 * 1000));
        assertEquals("1 hour", TimeUtil.formatDuration(60 * 60 * 1000));
        assertEquals("1 day", TimeUtil.formatDuration(24 * 60 * 60 * 1000));

        long complexDuration = (2 * 24 * 60 * 60 * 1000) + (3 * 60 * 60 * 1000) + (4 * 60 * 1000) + (5 * 1000);
        assertEquals("2 days, 3 hours, 4 minutes, 5 seconds", TimeUtil.formatDuration(complexDuration));

        // Test month and year formatting
        assertEquals("1 month", TimeUtil.formatDuration(30 * 24 * 60 * 60 * 1000L));
        assertEquals("2 months", TimeUtil.formatDuration(60 * 24 * 60 * 60 * 1000L));
        assertEquals("1 year", TimeUtil.formatDuration(365 * 24 * 60 * 60 * 1000L));
        assertEquals("2 years", TimeUtil.formatDuration(2 * 365 * 24 * 60 * 60 * 1000L));
    }
}