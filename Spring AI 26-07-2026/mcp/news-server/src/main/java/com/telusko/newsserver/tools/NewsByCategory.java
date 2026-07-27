package com.telusko.newsserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NewsByCategory
{

    @Value("${news.api.key}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    @Tool(description = "Get top news headlines for a specific category. " +
            "Use this when the user asks for news in a category like business, sports, " +
            "technology, health, science, or entertainment.")
    public String getNewsByCategory(
            @ToolParam(description = "News category: business, entertainment, general, " +
                    "health, science, sports, or technology") String category,
            @ToolParam(description = "2-letter country code such as 'us', 'in', 'gb'")
            String countryCode) {
        System.out.println("[Tool Called] getNewsByCategory: " + category + " / " + countryCode);
        String url = "https://newsapi.org/v2/top-headlines?category=" +
                category + "&country=" + countryCode +
                "&pageSize=5&apiKey=" + apiKey;
        try {
            String result = restTemplate.getForObject(url, String.class);
            return result != null ? result
                    : "No " + category + " news found for country " + countryCode;
        } catch (Exception e) {
            return "Error in fetching category news " + e.getMessage();
        }
    }

}
