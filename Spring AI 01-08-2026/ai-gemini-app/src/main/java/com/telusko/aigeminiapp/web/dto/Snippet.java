package com.telusko.aigeminiapp.web.dto;

import org.springframework.ai.document.Document;

public record Snippet(String source, Double score, String text) {

    public static Snippet from(Document document) {
        return new Snippet(
                String.valueOf(document.getMetadata().getOrDefault("source", "unknown")),
                document.getScore(),
                document.getText());
    }
}
