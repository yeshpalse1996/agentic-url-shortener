package com.agenticengineering.urlshortener.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.agenticengineering.urlshortener.dto.CreateUrlResponse;
import com.agenticengineering.urlshortener.entity.UrlMapping;
import com.agenticengineering.urlshortener.exception.UrlNotFoundException;
import com.agenticengineering.urlshortener.service.UrlMappingService;
import com.agenticengineering.urlshortener.service.UserAgentService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class UrlMappingControllerTest {

    @Mock
    private UrlMappingService service;

    @Mock
    private UserAgentService userAgentService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UrlMappingController controller;

    private UrlMapping mapping;

    @BeforeEach
    void setUp() {
        mapping = new UrlMapping();
        mapping.setShortCode("TCjYQht");
        mapping.setOriginalUrl("https://google.com");
    }

    @Test
    void shouldCreateShortUrlSuccessfully() {

        when(authentication.getName()).thenReturn("testuser");

        when(service.saveUrlMapping(
                any(UrlMapping.class), eq("testuser")))
                .thenAnswer(invocation -> {
                    UrlMapping input = invocation.getArgument(0);
                    input.setShortCode("TCjYQht");
                    return input;
                });

        CreateUrlResponse result = controller.createShortUrl(
                new UrlMappingController.CreateUrlRequest(
                        "https://google.com"),
                authentication);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("TCjYQht", result.shortCode());
        assertEquals("https://google.com", result.originalUrl());
        assertEquals(0L, result.clickCount());

        verify(authentication).getName();
        verify(service).saveUrlMapping(
                any(UrlMapping.class), eq("testuser"));
    }

    @Test
    void shouldRedirectToOriginalUrlAndTrackClick() {

        String userAgent = "test-user-agent";

        when(request.getHeader("User-Agent"))
                .thenReturn(userAgent);
        when(request.getHeader("Referer"))
                .thenReturn("https://example.com");

        when(userAgentService.getBrowser(userAgent))
                .thenReturn("Chrome");
        when(userAgentService.getOperatingSystem(userAgent))
                .thenReturn("Windows");
        when(userAgentService.getDeviceType(userAgent))
                .thenReturn("Desktop");

        when(service.trackClick(
                eq("TCjYQht"),
                eq("Chrome"),
                eq("Windows"),
                eq("Desktop"),
                eq("https://example.com")))
                .thenReturn(mapping);

        ResponseEntity<Void> response =
                controller.redirect("TCjYQht", request);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(
                "https://google.com",
                response.getHeaders().getLocation().toString());

        verify(service).trackClick(
                "TCjYQht",
                "Chrome",
                "Windows",
                "Desktop",
                "https://example.com");
    }

    @Test
    void shouldPropagateExceptionForUnknownShortCode() {

        String userAgent = "test-user-agent";

        when(request.getHeader("User-Agent"))
                .thenReturn(userAgent);
        when(request.getHeader("Referer"))
                .thenReturn(null);

        when(userAgentService.getBrowser(userAgent))
                .thenReturn("Chrome");
        when(userAgentService.getOperatingSystem(userAgent))
                .thenReturn("Windows");
        when(userAgentService.getDeviceType(userAgent))
                .thenReturn("Desktop");

        when(service.trackClick(
                "missing",
                "Chrome",
                "Windows",
                "Desktop",
                null))
                .thenThrow(
                        new UrlNotFoundException("Short URL not found"));

        assertThrows(
                UrlNotFoundException.class,
                () -> controller.redirect("missing", request));
    }
}