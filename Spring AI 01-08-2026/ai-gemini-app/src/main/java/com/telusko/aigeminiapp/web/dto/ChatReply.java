package com.telusko.aigeminiapp.web.dto;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

public record ChatReply(
        String conversationId,
        String reply,
        String model,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens
) {

    private static final String EMPTY_ANSWER =
            "The model returned an empty answer. Please ask again.";

    public static ChatReply from(String conversationId, ChatResponse response) {
        Usage usage = response.getMetadata().getUsage();

        return new ChatReply(
                conversationId,
                textOf(response),
                response.getMetadata().getModel(),
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens());
    }

    /**
     * Gemini occasionally finishes with reason STOP but no content at all - it spent the
     * whole turn on internal reasoning tokens. Rare, but a null here would reach the
     * browser as {@code "reply": null}, so turn it into something a human can read.
     */
    private static String textOf(ChatResponse response) {
        String text = response.getResult().getOutput().getText();
        return (text == null || text.isBlank()) ? EMPTY_ANSWER : text;
    }
}

