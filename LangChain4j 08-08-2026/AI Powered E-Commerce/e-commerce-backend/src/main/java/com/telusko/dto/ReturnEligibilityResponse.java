package com.telusko.dto;

/**
 * Whether an order can be returned, and why.
 * <p>
 * {@code eligible} and {@code reason} are decided in Java from the order's real status and date.
 * {@code explanation} is the model's customer-friendly wording of that same decision - it never
 * gets to change the outcome, because a chatbot promising a refund the policy does not allow is
 * exactly the failure that makes these features unusable in production.
 */
public record ReturnEligibilityResponse(
        Long orderId,
        String orderStatus,
        boolean eligible,
        String reason,
        String explanation
) {}
