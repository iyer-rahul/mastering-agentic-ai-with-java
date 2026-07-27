package com.telusko.newsserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NewsSearchTool
{
    @Value("${news.api.key}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    @Tool(description = "Get current news articles for a specific topic or keyword. " +
            "Use this when the user asks about news, articles, or recent updates on any subject.")
    public String getNews(@ToolParam(description = "Topic or keyword to search news for, " +
            "such as 'AI', 'India', 'SpaceX', 'technology'") String topic) {
        System.out.println("[Tool Called] getNews: " + topic);
        String url = "https://newsapi.org/v2/everything?q=" +
                topic.replace(" ", "+") +
                "&pageSize=5&sortBy=publishedAt&apiKey=" + apiKey;
        try {
            String result = restTemplate.getForObject(url, String.class);
            return result != null ? result : "No news found for the topic " + topic;
        } catch (Exception e) {
            return "Error in fetching news " + e.getMessage();
        }
    }
}
