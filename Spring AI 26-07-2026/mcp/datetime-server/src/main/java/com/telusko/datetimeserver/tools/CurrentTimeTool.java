package com.telusko.datetimeserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CurrentTimeTool
{
    @Tool(description = "Get the current date and time in the server's LOCAL timezone. " +
            "Use this when the user asks for the current time without specifying any location.")
    public String getCurrentDateAndTime() {
        System.out.println("[Tool Called] getCurrentDateAndTime (local timezone)");
        return ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
    }
}
