
package com.agenticengineering.urlshortener.service;

import java.util.Comparator;
import java.util.List;

import com.agenticengineering.urlshortener.dto.AnalyticsDashboardResponse;
import com.agenticengineering.urlshortener.dto.ClickStat;
import tools.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class UrlAiInsightsService {

    private static final Logger logger = LoggerFactory.getLogger(UrlAiInsightsService.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public UrlAiInsightsService(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String generateInsights(
            AnalyticsDashboardResponse dashboard) {

        String analyticsJson = objectMapper.writeValueAsString(dashboard);

        try {
            return chatClient.prompt()
                    .system("""
                            You are a URL analytics expert.
                            Summarize the supplied analytics data.
                            Identify useful trends and practical
                            recommendations.
                            Never invent statistics.
                            Mention when the data is insufficient.
                            Keep the response concise.
                            """)
                    .user("Analyze this URL analytics data:\n"
                            + analyticsJson)
                    .call()
                    .content();

        } catch (Exception exception) {
            logger.warn(
                    "OpenAI insights unavailable for short code {}. "
                            + "Using rule-based fallback. Reason: {}",
                    dashboard.shortCode(),
                    exception.getMessage());

            return generateRuleBasedInsights(dashboard);
        }
    }

    private String generateRuleBasedInsights(AnalyticsDashboardResponse dashboard) {
        StringBuilder insights = new StringBuilder();

        insights.append("URL Performance Insights (Rule-Based)\n\n");
        insights.append("Short code: ").append(dashboard.shortCode()).append("\n");
        insights.append("Original URL: ").append(dashboard.originalUrl()).append("\n");
        insights.append("Total clicks: ").append(dashboard.totalClicks()).append("\n\n");

        if (dashboard.totalClicks() == 0) {
            insights.append("Observation: No clicks have been recorded yet.\n");
            insights.append("Recommendation: Share this URL through relevant channels ");
            insights.append("and monitor future activity.\n");
            return insights.toString();
        }

        insights.append("1. CLICK DISTRIBUTION\n");

        appendCategoryInsights(
                insights, "Browser", dashboard.browserStats());

        appendCategoryInsights(
                insights, "Device", dashboard.deviceStats());

        appendCategoryInsights(
                insights, "Operating system",
                dashboard.operatingSystemStats());

        appendReferrerInsights(insights, dashboard);

        insights.append("\n2. PEAK ACTIVITY\n");

        if (dashboard.dailyClicks() == null
                || dashboard.dailyClicks().isEmpty()) {
            insights.append("Daily activity data is unavailable.\n");
        } else {
            var busiestDay = dashboard.dailyClicks().stream()
                    .max(Comparator.comparingLong(day -> day.clicks()))
                    .orElseThrow();

            insights.append("Busiest recorded day: ")
                    .append(busiestDay.date())
                    .append(" with ")
                    .append(busiestDay.clicks())
                    .append(" clicks.\n");
        }

        insights.append("\n3. RECOMMENDATIONS\n");

        appendRecommendations(insights, dashboard);

        return insights.toString();
    }

    private void appendCategoryInsights(
            StringBuilder insights,
            String category,
            List<ClickStat> stats) {

        if (stats == null || stats.isEmpty()) {
            insights.append(category).append(": No data available.\n");
            return;
        }

        long categoryTotal = stats.stream()
                .mapToLong(ClickStat::clicks)
                .sum();

        if (categoryTotal == 0) {
            insights.append(category).append(": No recorded data.\n");
            return;
        }

        insights.append(category).append(" distribution:\n");

        stats.stream()
                .sorted(Comparator.comparingLong(ClickStat::clicks).reversed())
                .forEach(stat -> {
                    double percentage = stat.clicks() * 100.0 / categoryTotal;

                    insights.append("- ")
                            .append(stat.name())
                            .append(": ")
                            .append(stat.clicks())
                            .append(" clicks (")
                            .append(String.format("%.1f", percentage))
                            .append("%)\n");
                });
    }

    private void appendReferrerInsights(
            StringBuilder insights,
            AnalyticsDashboardResponse dashboard) {

        var referrers = dashboard.referrerStats();

        if (referrers == null || referrers.isEmpty()) {
            insights.append("Referrers: No data available.\n");
            return;
        }

        long totalReferrerClicks = referrers.stream()
                .mapToLong(referrer -> referrer.clicks())
                .sum();

        insights.append("Referrer distribution:\n");

        referrers.stream()
                .sorted(Comparator.comparingLong(
                        (com.agenticengineering.urlshortener.dto.ReferrerStat r) -> r.clicks()).reversed())
                .forEach(referrer -> {
                    double percentage = totalReferrerClicks == 0
                            ? 0
                            : referrer.clicks() * 100.0 / totalReferrerClicks;

                    insights.append("- ")
                            .append(referrer.referrer())
                            .append(": ")
                            .append(referrer.clicks())
                            .append(" clicks (")
                            .append(String.format("%.1f", percentage))
                            .append("%)\n");
                });
    }

    private void appendRecommendations(
            StringBuilder insights,
            AnalyticsDashboardResponse dashboard) {

        List<ClickStat> devices = dashboard.deviceStats();

        if (devices != null && !devices.isEmpty()) {
            long totalDeviceClicks = devices.stream()
                    .mapToLong(ClickStat::clicks)
                    .sum();

            if (totalDeviceClicks > 0) {
                ClickStat topDevice = devices.stream()
                        .max(Comparator.comparingLong(ClickStat::clicks))
                        .orElseThrow();

                double percentage = topDevice.clicks() * 100.0 / totalDeviceClicks;

                insights.append("- ")
                        .append(String.format("%.1f", percentage))
                        .append("% of categorized device clicks are from ")
                        .append(topDevice.name()).append(". ");

                if (percentage >= 70) {
                    insights.append("Prioritize testing the user experience ")
                            .append("on this device category.\n");
                } else {
                    insights.append("Continue testing across device categories.\n");
                }
            }
        }

        List<ClickStat> browsers = dashboard.browserStats();

        if (browsers != null && !browsers.isEmpty()) {
            ClickStat topBrowser = browsers.stream()
                    .max(Comparator.comparingLong(ClickStat::clicks))
                    .orElseThrow();

            insights.append("- Verify compatibility with ")
                    .append(topBrowser.name())
                    .append(", the leading recorded browser category.\n");
        }

        if (dashboard.dailyClicks() == null
                || dashboard.dailyClicks().size() < 2) {
            insights.append("- Collect more daily data before drawing ")
                    .append("conclusions about traffic trends.\n");
        } else {
            insights.append("- Compare daily click counts over time ")
                    .append("to identify changes in traffic.\n");
        }

        if (dashboard.referrerStats() == null
                || dashboard.referrerStats().isEmpty()) {
            insights.append("- Capture referrer information where available ")
                    .append("to better understand traffic sources.\n");
        } else {
            insights.append("- Review recorded referrers to identify ")
                    .append("which channels are bringing visits.\n");
        }
    }

}
