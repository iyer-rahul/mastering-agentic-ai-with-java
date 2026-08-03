package com.telusko.aigeminiapp.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Telusko
{
    private ChatClient chatClient;

    public Telusko(ChatClient.Builder builder)
    {
        this.chatClient = builder.build();
    }
    @GetMapping("/api/chat/{message}")
    public String home(@PathVariable String message)
    {

              ChatResponse response= chatClient
                .prompt(message)
                .call()
                .chatResponse();
        System.out.println(response.getMetadata().getUsage().getTotalTokens());
              return response.getResult().getOutput().getText();
    }
}
