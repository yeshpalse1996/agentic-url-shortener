package com.agenticengineering.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.agenticengineering.urlshortener.dto.ClickHistoryResponse;
import com.agenticengineering.urlshortener.dto.UrlAnalyticsResponse;
import com.agenticengineering.urlshortener.entity.UrlClick;
import com.agenticengineering.urlshortener.entity.UrlMapping;
import com.agenticengineering.urlshortener.exception.UrlNotFoundException;
import com.agenticengineering.urlshortener.repository.UrlClickRepository;
import com.agenticengineering.urlshortener.repository.UrlMappingRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlAnalyticsServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private UrlClickRepository urlClickRepository;

    @InjectMocks
    private UrlAnalyticsService urlAnalyticsService;

    @Test
    void shouldReturnUrlAnalyticsSuccessfully() {

        String shortCode = "TCjYQht";
        LocalDateTime clickTime = LocalDateTime.now();

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(shortCode);
        mapping.setOriginalUrl("https://google.com");

        UrlClick click = new UrlClick(
                shortCode,
                clickTime,
                "Chrome",
                "Windows",
                "Desktop",
                "https://example.com"
        );

        when(urlMappingRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(mapping));

        when(urlClickRepository.countByShortCode(shortCode))
                .thenReturn(1L);

        when(urlClickRepository
                .findTopByShortCodeOrderByClickedAtDesc(shortCode))
                .thenReturn(Optional.of(click));

        UrlAnalyticsResponse response =
                urlAnalyticsService.getAnalytics(shortCode);

        assertEquals(shortCode, response.shortCode());
        assertEquals("https://google.com", response.originalUrl());
        assertEquals(1L, response.totalClicks());
        assertEquals(clickTime, response.lastClickedAt());

        verify(urlClickRepository).countByShortCode(shortCode);
    }

    @Test
    void shouldReturnClickHistorySuccessfully() {

        String shortCode = "TCjYQht";
        LocalDateTime clickTime = LocalDateTime.now();

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode(shortCode);
        mapping.setOriginalUrl("https://google.com");

        UrlClick click = new UrlClick(
                shortCode,
                clickTime,
                "Chrome",
                "Windows",
                "Desktop",
                "https://example.com"
        );

        when(urlMappingRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(mapping));

        when(urlClickRepository
                .findByShortCodeOrderByClickedAtDesc(shortCode))
                .thenReturn(List.of(click));

        List<ClickHistoryResponse> history =
                urlAnalyticsService.getClickHistory(shortCode);

        assertEquals(1, history.size());

        ClickHistoryResponse response = history.get(0);

        assertEquals(shortCode, response.shortCode());
        assertEquals(clickTime, response.clickedAt());
        assertEquals("Chrome", response.browser());
        assertEquals("Windows", response.operatingSystem());
        assertEquals("Desktop", response.deviceType());
        assertEquals("https://example.com", response.referrer());
    }

    @Test
    void shouldThrowExceptionForUnknownShortCode() {

        String shortCode = "missing";

        when(urlMappingRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        assertThrows(
                UrlNotFoundException.class,
                () -> urlAnalyticsService.getAnalytics(shortCode)
        );
    }
}