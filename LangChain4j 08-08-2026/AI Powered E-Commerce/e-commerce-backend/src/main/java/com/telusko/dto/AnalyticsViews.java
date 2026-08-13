package com.telusko.dto;

import java.math.BigDecimal;

/**
 * Small read-only shapes returned by the admin analytics tools.
 * <p>
 * The model sees these as the tool result, so the field names double as the explanation of what
 * each number means - "unitsSold" reads better in a generated answer than a bare column alias.
 */
public final class AnalyticsViews {

    private AnalyticsViews() {
    }

    public record SalesSummaryView(
            String period,
            long orders,
            BigDecimal revenue,
            BigDecimal averageOrderValue
    ) {}

    public record TopProductView(
            String productName,
            long unitsSold,
            BigDecimal revenue
    ) {}

    public record LowStockView(
            Long productId,
            String productName,
            Integer stockQty
    ) {}

    public record StatusCountView(
            String status,
            long orders
    ) {}

    public record CategoryRevenueView(
            String category,
            BigDecimal revenue
    ) {}
}
