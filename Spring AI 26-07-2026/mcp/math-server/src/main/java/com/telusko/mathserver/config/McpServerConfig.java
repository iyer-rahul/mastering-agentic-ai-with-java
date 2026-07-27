package com.telusko.mathserver.config;

import com.telusko.mathserver.tool.*;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpServerConfig
{
    // Each tool now lives in its own class. We register all of them here so the
    // math-server exposes 7 separate tools over MCP.
    @Bean
    public List<ToolCallback> toolCallbacks(
            AdditionTool additionTool,
            SubtractionTool subtractionTool,
            MultiplicationTool multiplicationTool,

            PowerTool powerTool,
            ModulusTool modulusTool)
    {
        return List.of(
                ToolCallbacks.from(
                        additionTool,
                        subtractionTool,
                        multiplicationTool,

                        powerTool,
                        modulusTool
                       )
        );
    }
}
