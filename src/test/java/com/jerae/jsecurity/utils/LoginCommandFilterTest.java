package com.jerae.jsecurity.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginCommandFilterTest {

    private LoginCommandFilter filter;

    @BeforeEach
    public void setUp() {
        filter = new LoginCommandFilter();
    }

    @Test
    public void testFilterLoginCommand() {
        LogEvent event = org.apache.logging.log4j.core.impl.Log4jLogEvent.newBuilder()
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Player issued server command: /login password123"))
                .build();

        assertEquals(AbstractFilter.Result.DENY, filter.filter(event));
    }

    @Test
    public void testFilterRegisterCommand() {
        LogEvent event = org.apache.logging.log4j.core.impl.Log4jLogEvent.newBuilder()
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Player issued server command: /register password123 password123"))
                .build();

        assertEquals(AbstractFilter.Result.DENY, filter.filter(event));
    }

    @Test
    public void testFilterOtherCommand() {
        LogEvent event = org.apache.logging.log4j.core.impl.Log4jLogEvent.newBuilder()
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("Player issued server command: /someothercommand"))
                .build();

        assertEquals(AbstractFilter.Result.NEUTRAL, filter.filter(event));
    }
}