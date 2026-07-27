package com.telusko.mathserver.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class SubtractionTool
{
    @Tool(description = "Subtract two numbers. Use for subtraction, minus, or difference operations.")
    public double subtract(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        System.out.println("[Tool Called] subtract: " + a + " - " + b);
        return a - b;
    }
}
