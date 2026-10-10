package com.agenticengineering.urlshortener.dto;

import java.time.LocalDateTime;

public record UrlAnalyticsResponse(
        String shortCode,
        String originalUrl,
        long totalClicks,
        LocalDateTime lastClickedAt
) {
}