package com.telusko.newsserver.config;

import com.telusko.newsserver.tools.NewsByCategory;
import com.telusko.newsserver.tools.NewsSearchTool;
import com.telusko.newsserver.tools.TopHeadlinesTool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpServerConfig
{
    // Each tool lives in its own class. The news-server exposes 3 separate tools.
    @Bean
    public List<ToolCallback> toolCallbacks(
            NewsSearchTool newsSearchTool,
            TopHeadlinesTool topHeadlinesTool,
            NewsByCategory newsByCategoryTool)
    {
        return List.of(
                ToolCallbacks.from(newsSearchTool, topHeadlinesTool, newsByCategoryTool)
        );
    }
}
