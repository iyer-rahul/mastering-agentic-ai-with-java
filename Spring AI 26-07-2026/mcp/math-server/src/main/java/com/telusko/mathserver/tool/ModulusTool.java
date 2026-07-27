package com.telusko.mathserver.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ModulusTool
{
    @Tool(description = "Get the remainder when dividing one number by another. " +
            "Use for modulus, modulo, or remainder operations.")
    public double modulus(
            @ToolParam(description = "The dividend") double a,
            @ToolParam(description = "The divisor") double b) {
        System.out.println("[Tool Called] modulus: " + a + " % " + b);
        return a % b;
    }
}
