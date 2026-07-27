package com.telusko.datetimeserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DayOfWeekTool
{
    @Tool(description = "Find out which day of the week a given date falls on. " +
            "Use this when the user asks what day a date is or was.")
    public String getDayOfWeek(
            @ToolParam(description = "A date in ISO format yyyy-MM-dd, such as 2026-07-25")
            String date) {
        System.out.println("[Tool Called] getDayOfWeek: " + date);
        try {
            return LocalDate.parse(date).getDayOfWeek().toString();
        } catch (Exception e) {
            return "Invalid date: " + date + ". Please use the format yyyy-MM-dd.";
        }
    }
}
