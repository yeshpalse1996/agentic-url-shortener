
package com.agenticengineering.urlshortener.controller;

import com.agenticengineering.urlshortener.dto.AnalyticsDashboardResponse;
import com.agenticengineering.urlshortener.dto.ClickHistoryResponse;
import com.agenticengineering.urlshortener.dto.DailyClickAnalytics;
import com.agenticengineering.urlshortener.dto.DeviceAnalyticsResponse;
import com.agenticengineering.urlshortener.dto.ReferrerStat;
import com.agenticengineering.urlshortener.dto.UrlAnalyticsResponse;
import com.agenticengineering.urlshortener.service.UrlAnalyticsService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(
    name = "Analytics",
    description = "View click history, traffic statistics, and URL performance"
)
@RestController
@RequestMapping("/api/urls")
public class UrlAnalyticsController {

    private final UrlAnalyticsService urlAnalyticsService;

    public UrlAnalyticsController(
            UrlAnalyticsService urlAnalyticsService) {
        this.urlAnalyticsService = urlAnalyticsService;
    }

    @Operation(
        summary = "Get URL analytics",
        description = "Returns the total click count and last-clicked timestamp for a short URL."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "URL analytics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UrlAnalyticsResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Short code not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/{shortCode}/analytics")
    public ResponseEntity<UrlAnalyticsResponse> getAnalytics(
            @PathVariable String shortCode) {

        return ResponseEntity.ok(
                urlAnalyticsService.getAnalytics(shortCode));
    }

    @Operation(
        summary = "Get daily click analytics",
        description = "Returns the number of clicks recorded for each day."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Daily click analytics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DailyClickAnalytics.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Short code not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/{shortCode}/analytics/daily")
    public ResponseEntity<List<DailyClickAnalytics>> getDailyAnalytics(
            @PathVariable String shortCode) {

        return ResponseEntity.ok(
                urlAnalyticsService.getDailyAnalytics(shortCode));
    }

    @Operation(
        summary = "Get click history",
        description = "Returns individual click records, including browser, operating system, device type, timestamp, and referrer."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Click history retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClickHistoryResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Short code not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/{shortCode}/clicks")
    public List<ClickHistoryResponse> getClickHistory(
            @PathVariable String shortCode) {

        return urlAnalyticsService.getClickHistory(shortCode);
    }

    @Operation(
        summary = "Get device and browser analytics",
        description = "Returns click statistics grouped by browser, device type, and operating system."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Device analytics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DeviceAnalyticsResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Short code not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/{shortCode}/analytics/devices")
    public DeviceAnalyticsResponse getDeviceAnalytics(
            @PathVariable String shortCode) {

        return urlAnalyticsService.getDeviceAnalytics(shortCode);
    }

    @Operation(
        summary = "Get referrer analytics",
        description = "Returns click counts grouped by the referring source."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Referrer analytics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReferrerStat.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Short code not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/{shortCode}/analytics/referrers")
    public List<ReferrerStat> getReferrerAnalytics(
            @PathVariable String shortCode) {

        return urlAnalyticsService.getReferrerAnalytics(shortCode);
    }

    @Operation(
        summary = "Get complete analytics dashboard",
        description = "Returns a combined view of total clicks, daily clicks, browser statistics, device statistics, operating systems, and referrers."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Analytics dashboard retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AnalyticsDashboardResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Short code not found"),
        @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/{shortCode}/analytics/dashboard")
    public AnalyticsDashboardResponse getDashboard(
            @PathVariable String shortCode) {

        return urlAnalyticsService.getDashboard(shortCode);
    }
}