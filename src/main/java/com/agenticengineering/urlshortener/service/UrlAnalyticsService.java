
package com.agenticengineering.urlshortener.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.agenticengineering.urlshortener.dto.AnalyticsDashboardResponse;
import com.agenticengineering.urlshortener.dto.ClickHistoryResponse;
import com.agenticengineering.urlshortener.dto.DailyClickAnalytics;
import com.agenticengineering.urlshortener.dto.DeviceAnalyticsResponse;
import com.agenticengineering.urlshortener.dto.ReferrerStat;
import com.agenticengineering.urlshortener.dto.UrlAnalyticsResponse;
import com.agenticengineering.urlshortener.entity.UrlClick;
import com.agenticengineering.urlshortener.entity.UrlMapping;
import com.agenticengineering.urlshortener.exception.UrlNotFoundException;
import com.agenticengineering.urlshortener.repository.UrlClickRepository;
import com.agenticengineering.urlshortener.repository.UrlMappingRepository;

@Service
public class UrlAnalyticsService {

    private static final Logger logger =
            LoggerFactory.getLogger(UrlAnalyticsService.class);

    private final UrlMappingRepository urlMappingRepository;
    private final UrlClickRepository urlClickRepository;

    public UrlAnalyticsService(
            UrlMappingRepository urlMappingRepository,
            UrlClickRepository urlClickRepository) {
        this.urlMappingRepository = urlMappingRepository;
        this.urlClickRepository = urlClickRepository;
    }

    private UrlMapping getExistingMapping(String shortCode) {
        return urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    logger.warn(
                            "Analytics requested for unknown short code: {}",
                            shortCode);
                    return new UrlNotFoundException(shortCode);
                });
    }

    public UrlAnalyticsResponse getAnalytics(String shortCode) {

        UrlMapping mapping = getExistingMapping(shortCode);

        long totalClicks = urlClickRepository.countByShortCode(shortCode);

        UrlClick lastClick = urlClickRepository
                .findTopByShortCodeOrderByClickedAtDesc(shortCode)
                .orElse(null);

        logger.info("Retrieved URL analytics for short code: {}", shortCode);

        return new UrlAnalyticsResponse(
                shortCode,
                mapping.getOriginalUrl(),
                totalClicks,
                lastClick != null ? lastClick.getClickedAt() : null);
    }

    public List<DailyClickAnalytics> getDailyAnalytics(String shortCode) {

        getExistingMapping(shortCode);

        List<Object[]> results = urlClickRepository.findDailyClicks(shortCode);

        List<DailyClickAnalytics> dailyAnalytics = results.stream()
                .map(row -> {
                    LocalDate date;

                    if (row[0] instanceof LocalDate localDate) {
                        date = localDate;
                    } else if (row[0] instanceof java.sql.Date sqlDate) {
                        date = sqlDate.toLocalDate();
                    } else {
                        throw new IllegalStateException(
                                "Unexpected date type: "
                                        + (row[0] == null
                                                ? "null"
                                                : row[0].getClass().getName()));
                    }

                    long clicks = ((Number) row[1]).longValue();

                    return new DailyClickAnalytics(date, clicks);
                })
                .toList();

        logger.info("Retrieved daily analytics for short code: {}", shortCode);

        return dailyAnalytics;
    }

    public List<ClickHistoryResponse> getClickHistory(String shortCode) {

        getExistingMapping(shortCode);

        List<UrlClick> clicks = urlClickRepository
                .findByShortCodeOrderByClickedAtDesc(shortCode);

        logger.info(
                "Retrieved click history for short code: {}. Records: {}",
                shortCode,
                clicks.size());

        return clicks.stream()
                .map(click -> new ClickHistoryResponse(
                        click.getShortCode(),
                        click.getClickedAt(),
                        click.getBrowser(),
                        click.getOperatingSystem(),
                        click.getDeviceType(),
                        click.getReferrer()))
                .toList();
    }

    public DeviceAnalyticsResponse getDeviceAnalytics(String shortCode) {

        getExistingMapping(shortCode);

        logger.info("Retrieved device analytics for short code: {}", shortCode);

        return new DeviceAnalyticsResponse(
                shortCode,
                urlClickRepository.countClicksByBrowser(shortCode),
                urlClickRepository.countClicksByDevice(shortCode),
                urlClickRepository.countClicksByOperatingSystem(shortCode));
    }

    public List<ReferrerStat> getReferrerAnalytics(String shortCode) {

        getExistingMapping(shortCode);

        logger.info("Retrieved referrer analytics for short code: {}", shortCode);

        return urlClickRepository.countClicksByReferrer(shortCode);
    }

    public AnalyticsDashboardResponse getDashboard(String shortCode) {

        UrlMapping mapping = getExistingMapping(shortCode);

        UrlAnalyticsResponse analytics = getAnalytics(shortCode);

        AnalyticsDashboardResponse dashboard = new AnalyticsDashboardResponse(
                shortCode,
                mapping.getOriginalUrl(),
                analytics.totalClicks(),
                analytics.lastClickedAt(),
                getDailyAnalytics(shortCode),
                urlClickRepository.countClicksByBrowser(shortCode),
                urlClickRepository.countClicksByDevice(shortCode),
                urlClickRepository.countClicksByOperatingSystem(shortCode),
                urlClickRepository.countClicksByReferrer(shortCode));

        logger.info("Retrieved analytics dashboard for short code: {}", shortCode);

        return dashboard;
    }
}
