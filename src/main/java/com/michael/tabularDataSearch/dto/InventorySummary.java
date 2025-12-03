package com.michael.tabularDataSearch.dto;

public record InventorySummary(
        String category,
        long productCount,
        long totalQuantity,
        double averagePrice
) {
}
