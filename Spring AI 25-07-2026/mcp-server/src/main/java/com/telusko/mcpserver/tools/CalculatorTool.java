package com.telusko.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTool
{
    @Tool(description = "Add two numbers. Use this when the user asks for addition, sum, or plus operation.")
    public double add(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        System.out.println("[Tool Called] add: " + a + " + " + b);
        return a + b;
    }

    @Tool(description = "Subtract two numbers. Use for subtraction or minus operations.")
    public double subtract(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        System.out.println("[Tool Called] subtract: " + a + " - " + b);
        return a - b;
    }

    @Tool(description = "Multiply two numbers. Use for multiplication or times operations.")
    public double multiply(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        System.out.println("[Tool Called] multiply: " + a + " * " + b);
        return a * b;
    }

    @Tool(description = "Divide two numbers. Use for division operations. " +
            "Returns error if dividing by zero.")
    public String divide(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        System.out.println("[Tool Called] divide: " + a + " / " + b);
        if (b == 0) return "Error: Cannot divide by zero";
        return String.valueOf(a / b);
    }

}
