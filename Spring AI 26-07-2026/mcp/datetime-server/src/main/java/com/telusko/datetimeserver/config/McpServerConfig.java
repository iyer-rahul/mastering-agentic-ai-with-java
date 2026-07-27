package com.telusko.datetimeserver.config;

import com.telusko.datetimeserver.tools.CurrentTimeTool;
import com.telusko.datetimeserver.tools.DayOfWeekTool;
import com.telusko.datetimeserver.tools.TimeZoneTimeTool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpServerConfig
{
    // Each tool lives in its own class. The datetime-server exposes 3 separate tools.
    @Bean
    public List<ToolCallback> toolCallbacks(
            CurrentTimeTool currentTimeTool,
            TimeZoneTimeTool timeZoneTimeTool,
            DayOfWeekTool dayOfWeekTool)
    {
        return List.of(
                ToolCallbacks.from(currentTimeTool, timeZoneTimeTool, dayOfWeekTool)
        );
    }
}
