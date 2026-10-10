
package com.agenticengineering.urlshortener.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "url_clicks")
public class UrlClick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shortCode;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    private String browser;

    private String operatingSystem;

    private String deviceType;

    @Column(length = 1000)
    private String referrer;

    public UrlClick() {
    }

    public UrlClick(String shortCode, LocalDateTime clickedAt) {
        this.shortCode = shortCode;
        this.clickedAt = clickedAt;
    }

    public UrlClick(
            String shortCode,
            LocalDateTime clickedAt,
            String browser,
            String operatingSystem,
            String deviceType,
            String referrer) {

        this.shortCode = shortCode;
        this.clickedAt = clickedAt;
        this.browser = browser;
        this.operatingSystem = operatingSystem;
        this.deviceType = deviceType;
        this.referrer = referrer;
    }

    public Long getId() {
        return id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public LocalDateTime getClickedAt() {
        return clickedAt;
    }

    public String getBrowser() {
        return browser;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getReferrer() {
        return referrer;
    }
}
