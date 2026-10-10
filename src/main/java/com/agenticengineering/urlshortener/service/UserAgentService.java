
package com.agenticengineering.urlshortener.service;

import org.springframework.stereotype.Service;

@Service
public class UserAgentService {

    public String getBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("edg/")) {
            return "Edge";
        }
        if (ua.contains("opr/") || ua.contains("opera")) {
            return "Opera";
        }
        if (ua.contains("firefox/")) {
            return "Firefox";
        }
        if (ua.contains("chrome/") && !ua.contains("edg/")) {
            return "Chrome";
        }
        if (ua.contains("safari/") && !ua.contains("chrome/")) {
            return "Safari";
        }

        return "Other";
    }

    public String getOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("windows")) {
            return "Windows";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("iphone") || ua.contains("ipad")
                || ua.contains("ios")) {
            return "iOS";
        }
        if (ua.contains("mac os") || ua.contains("macintosh")) {
            return "macOS";
        }
        if (ua.contains("linux")) {
            return "Linux";
        }

        return "Other";
    }

    public String getDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("ipad") || ua.contains("tablet")) {
            return "Tablet";
        }
        if (ua.contains("mobile") || ua.contains("iphone")
                || ua.contains("android")) {
            return "Mobile";
        }

        return "Desktop";
    }
}
