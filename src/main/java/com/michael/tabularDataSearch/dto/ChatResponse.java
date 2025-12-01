package com.michael.tabularDataSearch.dto;

import java.util.List;

public record ChatResponse(String answer, List<String> relevantSources) {
} 