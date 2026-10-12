package com.agenticengineering.urlshortener.dto;

import java.time.LocalDateTime;

public record CreateUrlResponse(
        Long id,
        String originalUrl,
        String shortCode,
        long clickCount,
        LocalDateTime createdAt
) {}