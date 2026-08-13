package com.telusko.dto;

import com.telusko.enums.TicketCategory;
import com.telusko.enums.TicketPriority;

/**
 * What the model is asked to produce when a new support ticket arrives.
 * <p>
 * Declaring it as a record lets Spring AI generate the JSON schema and bind the reply straight
 * back into typed fields, so the enums are validated for us instead of arriving as free text
 * that would then have to be parsed and second-guessed.
 */
public record TicketTriageResult(
        TicketCategory category,
        TicketPriority priority,
        String summary,
        String suggestedReply
) {}
