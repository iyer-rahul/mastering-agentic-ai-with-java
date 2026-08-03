package com.telusko.aigeminiapp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
public class DateTimeTools {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter HUMAN =
            DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy 'at' hh:mm a");

    @Tool(description = """
            Get the current date and time in India (Asia/Kolkata).
            Call this whenever the answer depends on "today", "now", "this week" or similar.""")
    public String currentDateTime() {
        return LocalDateTime.now(ZONE).format(HUMAN);
    }

    @Tool(description = """
            Count the days between today and a future or past date.
            Useful for questions like "how long until the batch starts?".""")
    public String daysUntil(
            @ToolParam(description = "Target date in yyyy-MM-dd format, e.g. 2026-09-01")
            String date) {

        try {
            LocalDate target = LocalDate.parse(date);
            long days = ChronoUnit.DAYS.between(LocalDate.now(ZONE), target);

            if (days == 0) {
                return date + " is today.";
            }
            return days > 0
                    ? days + " day(s) from today until " + date + "."
                    : Math.abs(days) + " day(s) have already passed since " + date + ".";
        }
        catch (DateTimeException e) {
            return "'" + date + "' is not a valid date. Expected the format yyyy-MM-dd.";
        }
    }
}

