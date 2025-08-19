package com.llm.tool_calling.currenttime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeTools {

    private static final Logger log =
        LoggerFactory.getLogger(DateTimeTools.class);

    @Tool(
            description = "Get the current date and time in the user's timezone"
    )
    public String getCurrentDateTimeWithoutZone() {
        log.info("DateTimeTools is invoked - getCurrentDateTime ");
        return LocalDateTime.now()
                .atZone(LocaleContextHolder.getTimeZone().toZoneId())
                .toString();
    }

    @Tool(
            description = "Get the current date and time in the specified timezone"
    )
    public String getCurrentDateTime(String timeZone) {
        log.info("DateTimeTools is invoked - getCurrentDateTime timeZone : {} ", timeZone);
        try{
            var zoneId = ZoneId.of(timeZone);
            var zonedDateTime = ZonedDateTime.now(zoneId);
            return zonedDateTime.toString();
        }catch (Exception e){
            log.error("Invalid time zone provided: {}", timeZone, e);
            return "Invalid time zone: " + timeZone;
        }

    }
}
