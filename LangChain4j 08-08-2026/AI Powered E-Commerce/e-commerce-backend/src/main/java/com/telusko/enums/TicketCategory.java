package com.telusko.enums;

/**
 * What a support ticket is actually about, so the queue can be routed and filtered.
 * Kept deliberately small - a long list makes classification unreliable.
 */
public enum TicketCategory {
    ORDER_ISSUE,
    DELIVERY,
    PAYMENT,
    RETURN_REFUND,
    PRODUCT_QUALITY,
    ACCOUNT,
    OTHER
}
