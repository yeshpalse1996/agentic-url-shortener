package com.agenticengineering.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserAgentServiceTest {

    private UserAgentService userAgentService;

    @BeforeEach
    void setUp() {
        userAgentService = new UserAgentService();
    }

    @Test
    void shouldDetectChromeBrowser() {
        String userAgent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/130.0.0.0 Safari/537.36";

        assertEquals("Chrome",
                userAgentService.getBrowser(userAgent));
    }

    @Test
    void shouldDetectEdgeBrowser() {
        String userAgent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0";

        assertEquals("Edge",
                userAgentService.getBrowser(userAgent));
    }

    @Test
    void shouldDetectWindowsOperatingSystem() {
        String userAgent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 Chrome/130.0.0.0 Safari/537.36";

        assertEquals("Windows",
                userAgentService.getOperatingSystem(userAgent));
    }

    @Test
    void shouldDetectAndroidMobileDevice() {
        String userAgent =
                "Mozilla/5.0 (Linux; Android 14; Pixel 8) "
                + "AppleWebKit/537.36 Chrome/130.0.0.0 Mobile Safari/537.36";

        assertEquals("Android",
                userAgentService.getOperatingSystem(userAgent));

        assertEquals("Mobile",
                userAgentService.getDeviceType(userAgent));
    }

    @Test
    void shouldDetectIphone() {
        String userAgent =
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) "
                + "AppleWebKit/605.1.15 (KHTML, like Gecko) "
                + "Version/17.0 Mobile/15E148 Safari/604.1";

        assertEquals("iOS",
                userAgentService.getOperatingSystem(userAgent));

        assertEquals("Mobile",
                userAgentService.getDeviceType(userAgent));
    }

    @Test
    void shouldHandleNullAndEmptyUserAgent() {
        assertEquals("Unknown",
                userAgentService.getBrowser(null));

        assertEquals("Unknown",
                userAgentService.getOperatingSystem(""));

        assertEquals("Unknown",
                userAgentService.getDeviceType(null));
    }
}