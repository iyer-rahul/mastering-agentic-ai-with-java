package com.telusko.mathserver.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class PowerTool {

    @Tool(description = "Raise a number to the power of an exponent. " +
            "Use for power, exponent, squared, or cubed operations.")
    public double power(
            @ToolParam(description = "The base number") double base,
            @ToolParam(description = "The exponent") double exponent) {
        System.out.println("[Tool Called] power: " + base + " ^ " + exponent);
        return Math.pow(base, exponent);
    }
}

