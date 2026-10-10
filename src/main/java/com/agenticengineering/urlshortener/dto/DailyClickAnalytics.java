package com.agenticengineering.urlshortener.dto;

import java.time.LocalDate;

public record DailyClickAnalytics(
        LocalDate date,
        long clicks
) {
}
