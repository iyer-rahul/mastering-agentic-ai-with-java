package com.telusko.aigeminiapp.web.dto;

import java.util.UUID;

public record ChatRequest(String message, String conversationId) {

    public String conversationIdOrNew() {
        return (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString()
                : conversationId;
    }
}
