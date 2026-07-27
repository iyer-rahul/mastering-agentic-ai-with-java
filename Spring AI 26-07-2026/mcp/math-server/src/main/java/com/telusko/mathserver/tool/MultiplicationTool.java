package com.telusko.mathserver.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class MultiplicationTool
{
    @Tool(description = "Multiply two numbers. Use for multiplication, times, or product operations.")
    public double multiply(
        @ToolParam(description = "First number") double a,
        @ToolParam(description = "Second number") double b) {
    System.out.println("[Tool Called] multiply: " + a + " * " + b);
    return a * b;
}
}
