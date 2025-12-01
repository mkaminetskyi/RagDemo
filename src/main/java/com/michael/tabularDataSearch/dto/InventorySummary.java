package com.michael.tabularDataSearch.dto;

/**
 * Aggregated inventory details grouped by product category.
 */
public record InventorySummary(
        String category,
        long productCount,
        long totalQuantity,
        double averagePrice
) {
}
