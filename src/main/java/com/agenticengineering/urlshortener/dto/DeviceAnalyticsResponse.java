
package com.agenticengineering.urlshortener.dto;

import java.util.List;

public record DeviceAnalyticsResponse(
        String shortCode,
        List<ClickStat> browserStats,
        List<ClickStat> deviceStats,
        List<ClickStat> operatingSystemStats
) {}
