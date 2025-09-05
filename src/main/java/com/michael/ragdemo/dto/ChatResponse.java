package com.michael.ragdemo.dto;

import java.util.List;

public record ChatResponse(String answer, List<String> relevantSources) {
} 