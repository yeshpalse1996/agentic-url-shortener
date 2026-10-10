
package com.agenticengineering.urlshortener.dto;

import java.time.LocalDateTime;

public record ClickHistoryResponse(
        String shortCode,
        LocalDateTime clickedAt,
        String browser,
        String operatingSystem,
        String deviceType,
        String referrer
) {}
