package com.agenticengineering.urlshortener.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.agenticengineering.urlshortener.dto.AnalyticsDashboardResponse;
import com.agenticengineering.urlshortener.dto.ClickHistoryResponse;
import com.agenticengineering.urlshortener.dto.ClickStat;
import com.agenticengineering.urlshortener.dto.DailyClickAnalytics;
import com.agenticengineering.urlshortener.dto.DeviceAnalyticsResponse;
import com.agenticengineering.urlshortener.dto.ReferrerStat;
import com.agenticengineering.urlshortener.dto.UrlAnalyticsResponse;
import com.agenticengineering.urlshortener.service.UrlAnalyticsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UrlAnalyticsController.class)
class UrlAnalyticsControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlAnalyticsService urlAnalyticsService;

    @Test
    void shouldReturnUrlAnalytics() throws Exception {

        when(urlAnalyticsService.getAnalytics("TCjYQht"))
                .thenReturn(new UrlAnalyticsResponse(
                        "TCjYQht",
                        "https://google.com",
                        5L,
                        LocalDateTime.of(2026, 10, 12, 10, 30)));

        mockMvc.perform(get("/api/urls/TCjYQht/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("TCjYQht"))
                .andExpect(jsonPath("$.originalUrl")
                        .value("https://google.com"))
                .andExpect(jsonPath("$.totalClicks").value(5))
                .andExpect(jsonPath("$.lastClickedAt")
                        .value("2026-10-12T10:30:00"));
    }

    @Test
    void shouldReturnDailyAnalytics() throws Exception {

        when(urlAnalyticsService.getDailyAnalytics("TCjYQht"))
                .thenReturn(List.of(
                        new DailyClickAnalytics(
                                LocalDate.of(2026, 10, 12), 5L)));

        mockMvc.perform(
                get("/api/urls/TCjYQht/analytics/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-10-12"))
                .andExpect(jsonPath("$[0].clicks").value(5));
    }

    @Test
    void shouldReturnClickHistory() throws Exception {

        when(urlAnalyticsService.getClickHistory("TCjYQht"))
                .thenReturn(List.of(
                        new ClickHistoryResponse(
                                "TCjYQht",
                                LocalDateTime.of(2026, 10, 12, 10, 30),
                                "Chrome",
                                "Windows",
                                "Desktop",
                                "https://example.com")));

        mockMvc.perform(get("/api/urls/TCjYQht/clicks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortCode").value("TCjYQht"))
                .andExpect(jsonPath("$[0].browser").value("Chrome"))
                .andExpect(jsonPath("$[0].operatingSystem").value("Windows"))
                .andExpect(jsonPath("$[0].deviceType").value("Desktop"))
                .andExpect(jsonPath("$[0].referrer")
                        .value("https://example.com"));
    }

    @Test
    void shouldReturnDeviceAnalytics() throws Exception {

        when(urlAnalyticsService.getDeviceAnalytics("TCjYQht"))
                .thenReturn(new DeviceAnalyticsResponse(
                        "TCjYQht",
                        List.of(new ClickStat("Chrome", 5L)),
                        List.of(new ClickStat("Desktop", 5L)),
                        List.of(new ClickStat("Windows", 5L))));

        mockMvc.perform(
                get("/api/urls/TCjYQht/analytics/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("TCjYQht"))
                .andExpect(jsonPath("$.browserStats[0].name")
                        .value("Chrome"))
                .andExpect(jsonPath("$.deviceStats[0].name")
                        .value("Desktop"))
                .andExpect(jsonPath("$.operatingSystemStats[0].name")
                        .value("Windows"));
    }

    @Test
    void shouldReturnReferrerAnalytics() throws Exception {

        when(urlAnalyticsService.getReferrerAnalytics("TCjYQht"))
                .thenReturn(List.of(
                        new ReferrerStat("https://example.com", 3L)));

        mockMvc.perform(
                get("/api/urls/TCjYQht/analytics/referrers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].referrer")
                        .value("https://example.com"))
                .andExpect(jsonPath("$[0].clicks").value(3));
    }

    @Test
    void shouldReturnAnalyticsDashboard() throws Exception {

        when(urlAnalyticsService.getDashboard("TCjYQht"))
                .thenReturn(mock(
                        AnalyticsDashboardResponse.class));

        mockMvc.perform(
                get("/api/urls/TCjYQht/analytics/dashboard"))
                .andExpect(status().isOk());
    }

    private static <T> T mock(Class<T> type) {
        return org.mockito.Mockito.mock(type);
    }
}