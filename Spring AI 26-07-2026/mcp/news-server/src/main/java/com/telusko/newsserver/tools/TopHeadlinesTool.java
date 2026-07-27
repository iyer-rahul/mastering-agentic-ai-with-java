package com.telusko.newsserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TopHeadlinesTool
{
    @Value("${news.api.key}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    @Tool(description = "Get the top breaking news headlines for a country. " +
            "Use this when the user asks for the latest, top, or breaking headlines.")
    public String getTopHeadlines(@ToolParam(description = "2-letter country code such as " +
            "'us', 'in', 'gb'") String countryCode) {
        System.out.println("[Tool Called] getTopHeadlines: " + countryCode);
        String url = "https://newsapi.org/v2/top-headlines?country=" +
                countryCode +
                "&pageSize=5&apiKey=" + apiKey;
        try {
            String result = restTemplate.getForObject(url, String.class);
            return result != null ? result : "No headlines found for country " + countryCode;
        } catch (Exception e) {
            return "Error in fetching headlines " + e.getMessage();
        }
    }



}
