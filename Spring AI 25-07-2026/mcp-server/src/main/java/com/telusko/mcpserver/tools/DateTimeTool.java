package com.telusko.mcpserver.tools;

import org.springframework.stereotype.Component;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeTool {

    @Tool(description = "Get the current date and time in the server's LOCAL timezone. " +
            "Use this when the user asks for current time without specifying any location.")
    public String getCurrentDateAndTime() {
        System.out.println("[Tool Called] getCurrentDateAndTime (local timezone)");
        return ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
    }

    @Tool(description = "Get the current date and time in a SPECIFIC timezone. " +
            "Use this when the user mentions a city, country, or timezone. " +
            "The timezone parameter must be a valid IANA timezone ID like " +
            "'Asia/Kolkata', 'America/New_York', 'Europe/London', 'Asia/Tokyo'.")
    public String getCurrentDateAndTimeTimeZoned(
            @ToolParam(description = "IANA timezone ID such as Asia/Kolkata or America/New_York")
            String timezone) {
        System.out.println("[Tool Called] getCurrentDateAndTimeTimeZoned: " + timezone);
        try {
            return ZonedDateTime.now(ZoneId.of(timezone))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        } catch (Exception e) {
            return "Invalid timezone: " + timezone +
                    ". Please use IANA format like Asia/Kolkata.";
        }
    }
}

