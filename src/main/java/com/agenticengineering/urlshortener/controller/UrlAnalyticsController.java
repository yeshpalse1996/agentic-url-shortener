package com.agenticengineering.urlshortener.controller;

import com.agenticengineering.urlshortener.dto.AnalyticsDashboardResponse;
import com.agenticengineering.urlshortener.dto.ClickHistoryResponse;
import com.agenticengineering.urlshortener.dto.DailyClickAnalytics;
import com.agenticengineering.urlshortener.dto.DeviceAnalyticsResponse;
import com.agenticengineering.urlshortener.dto.ReferrerStat;
import com.agenticengineering.urlshortener.dto.UrlAnalyticsResponse;
import com.agenticengineering.urlshortener.service.UrlAnalyticsService;
import com.agenticengineering.urlshortener.service.UrlAiInsightsService;
import com.agenticengineering.urlshortener.service.UrlMappingService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "Analytics", description = "View click history, traffic statistics, and URL performance")
@RestController
@RequestMapping("/api/urls")
public class UrlAnalyticsController {

        private final UrlAnalyticsService urlAnalyticsService;
        private final UrlAiInsightsService urlAiInsightsService;
        private final UrlMappingService urlMappingService;

        public UrlAnalyticsController(
                        UrlAnalyticsService urlAnalyticsService,
                        UrlAiInsightsService urlAiInsightsService,
                        UrlMappingService urlMappingService) {

                this.urlAnalyticsService = urlAnalyticsService;
                this.urlAiInsightsService = urlAiInsightsService;
                this.urlMappingService = urlMappingService;
        }

        private void verifyOwnership(
                        String shortCode,
                        Authentication authentication) {

                String username = authentication.getName();

                System.out.println("Analytics requested for: " + shortCode);
                System.out.println("Authenticated username: " + username);

                urlMappingService.getOwnedUrlMapping(shortCode, username);

                System.out.println("Ownership verification successful");
        }

        @Operation(summary = "Get URL analytics", description = "Returns click statistics for a URL owned by the authenticated user.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UrlAnalyticsResponse.class))),
                        @ApiResponse(responseCode = "403", description = "URL belongs to another user"),
                        @ApiResponse(responseCode = "404", description = "Short code not found")
        })
        @GetMapping("/{shortCode}/analytics")
        public ResponseEntity<UrlAnalyticsResponse> getAnalytics(
                        @PathVariable String shortCode,
                        Authentication authentication) {

                verifyOwnership(shortCode, authentication);

                return ResponseEntity.ok(
                                urlAnalyticsService.getAnalytics(shortCode));
        }

        @Operation(summary = "Get daily click analytics", description = "Returns daily click counts for a URL owned by the authenticated user.")
        @GetMapping("/{shortCode}/analytics/daily")
        public ResponseEntity<List<DailyClickAnalytics>> getDailyAnalytics(
                        @PathVariable String shortCode,
                        Authentication authentication) {

                verifyOwnership(shortCode, authentication);

                return ResponseEntity.ok(
                                urlAnalyticsService.getDailyAnalytics(shortCode));
        }

        @Operation(summary = "Get click history", description = "Returns click records for a URL owned by the authenticated user.")
        @GetMapping("/{shortCode}/clicks")
        public List<ClickHistoryResponse> getClickHistory(
                        @PathVariable String shortCode,
                        Authentication authentication) {

                verifyOwnership(shortCode, authentication);

                return urlAnalyticsService.getClickHistory(shortCode);
        }

        @Operation(summary = "Get device and browser analytics", description = "Returns browser, device, and operating-system statistics.")
        @GetMapping("/{shortCode}/analytics/devices")
        public DeviceAnalyticsResponse getDeviceAnalytics(
                        @PathVariable String shortCode,
                        Authentication authentication) {

                verifyOwnership(shortCode, authentication);

                return urlAnalyticsService.getDeviceAnalytics(shortCode);
        }

        @Operation(summary = "Get referrer analytics", description = "Returns click counts grouped by referring source.")
        @GetMapping("/{shortCode}/analytics/referrers")
        public List<ReferrerStat> getReferrerAnalytics(
                        @PathVariable String shortCode,
                        Authentication authentication) {

                verifyOwnership(shortCode, authentication);

                return urlAnalyticsService.getReferrerAnalytics(shortCode);
        }

        @Operation(summary = "Get complete analytics dashboard", description = "Returns combined click, device, browser, operating-system, and referrer statistics.")
        @GetMapping("/{shortCode}/analytics/dashboard")
        public AnalyticsDashboardResponse getDashboard(
                        @PathVariable String shortCode,
                        Authentication authentication) {

                verifyOwnership(shortCode, authentication);

                return urlAnalyticsService.getDashboard(shortCode);
        }

        @Operation(summary = "Get AI analytics insights", description = "Generates insights for a URL owned by the authenticated user.")
        @GetMapping("/{shortCode}/analytics/ai")
        public ResponseEntity<String> getAiInsights(
                        @PathVariable String shortCode,
                        Authentication authentication) {

                verifyOwnership(shortCode, authentication);

                AnalyticsDashboardResponse dashboard = urlAnalyticsService.getDashboard(shortCode);

                String insights = urlAiInsightsService.generateInsights(dashboard);

                return ResponseEntity.ok(insights);
        }
}