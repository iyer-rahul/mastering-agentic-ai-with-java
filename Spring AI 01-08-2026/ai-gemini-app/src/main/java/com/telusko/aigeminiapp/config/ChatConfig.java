package com.telusko.aigeminiapp.config;

import com.telusko.aigeminiapp.tools.CourseTools;
import com.telusko.aigeminiapp.tools.DateTimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig
{

    private static final String SYSTEM_PROMPT = """
            You are "Telusko Assistant", a friendly guide for the Telusko course platform.

            How you work:
            - Anything about our catalog comes from a tool call, never from memory.
              Before you name a single course, price, seat count or today's date,
              call the matching tool - even if you think you already know the answer.
            - If a tool returns nothing, say so plainly. Never invent a course or a price.
            - Before enrolling anybody, make sure you know the exact course id AND the
              student's name. Ask for whatever is missing instead of assuming.
            - General programming questions are fine to answer from your own knowledge.
              Only catalog facts are tool-only.
            - Keep replies short and conversational. Prices are in INR.
            """;

    /**
     * Remembers the last 20 messages per conversation id, in RAM.
     * Swap {@link InMemoryChatMemoryRepository} for the JDBC one and the same code
     * survives a restart - that is the only change needed.
     */
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder,
                          ChatMemory chatMemory,
                          CourseTools courseTools,
                          DateTimeTools dateTimeTools) {

        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        // replays the conversation history back into every request
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // prints the exact prompt + response at DEBUG level
                        new SimpleLoggerAdvisor())
                // these two objects become the model's callable functions
                .defaultTools(courseTools, dateTimeTools)
                .build();
    }
}
