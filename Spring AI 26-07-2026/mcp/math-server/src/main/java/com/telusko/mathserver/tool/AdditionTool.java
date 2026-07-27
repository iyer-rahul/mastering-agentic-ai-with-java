package com.telusko.mathserver.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class AdditionTool
{
    @Tool(description = "Add two numbers. Use this when the user asks for addition, sum, or plus operation.")
    public double add(
            @ToolParam(description = "First number") double a,
            @ToolParam(description = "Second number") double b) {
        System.out.println("[Tool Called] add: " + a + " + " + b);
        return a + b;
    }
}
