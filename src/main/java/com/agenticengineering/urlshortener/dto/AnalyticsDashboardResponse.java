
package com.agenticengineering.urlshortener.dto;

import java.util.List;
import java.time.LocalDateTime;

public record AnalyticsDashboardResponse(
        String shortCode,
        String originalUrl,
        long totalClicks,
        LocalDateTime lastClickedAt,
        List<DailyClickAnalytics> dailyClicks,
        List<ClickStat> browserStats,
        List<ClickStat> deviceStats,
        List<ClickStat> operatingSystemStats,
        List<ReferrerStat> referrerStats
) {}
