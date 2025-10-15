package com.jerae.jsecurity.utils;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;

public class LoginCommandFilter extends AbstractFilter {

    @Override
    public Result filter(LogEvent event) {
        String message = event.getMessage().getFormattedMessage();

        if (message != null) {
            String lowerCaseMessage = message.toLowerCase();
            if (lowerCaseMessage.contains("issued server command: /login") ||
                lowerCaseMessage.contains("issued server command: /register") ||
                lowerCaseMessage.contains("issued server command: /unregister") ||
                lowerCaseMessage.contains("issued server command: /changepass")) {
                return Result.DENY;
            }
        }

        return Result.NEUTRAL;
    }
}