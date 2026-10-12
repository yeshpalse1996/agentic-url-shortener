
package com.agenticengineering.urlshortener.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.agenticengineering.urlshortener.dto.CreateUrlResponse;
import com.agenticengineering.urlshortener.entity.UrlMapping;
import com.agenticengineering.urlshortener.service.UrlMappingService;
import com.agenticengineering.urlshortener.service.UserAgentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.Authentication;

@Tag(name = "URL Management", description = "Create and manage shortened URLs")
@RestController
@RequestMapping("/api/urls")
public class UrlMappingController {

    private final UrlMappingService service;
    private final UserAgentService userAgentService;

    public UrlMappingController(
            UrlMappingService service,
            UserAgentService userAgentService) {
        this.service = service;
        this.userAgentService = userAgentService;
    }

    public record CreateUrlRequest(
            @NotBlank(message = "URL is required") @Pattern(regexp = "^https?://.+", message = "URL must start with http:// or https://") String originalUrl) {
    }

    @Operation(tags = {
            "URL Management" }, summary = "Create a short URL", description = "Creates a short code for the supplied original URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Short URL created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UrlMapping.class))),
            @ApiResponse(responseCode = "400", description = "Invalid URL or request validation failed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping
    public CreateUrlResponse createShortUrl(
            @Valid @RequestBody CreateUrlRequest request,Authentication authentication) {

       UrlMapping mapping = new UrlMapping();
    mapping.setOriginalUrl(request.originalUrl());

    UrlMapping saved = service.saveUrlMapping(
            mapping, authentication.getName());

    return new CreateUrlResponse(
            saved.getId(),
            saved.getOriginalUrl(),
            saved.getShortCode(),
            saved.getClickCount(),
            saved.getCreatedAt()
    );
    }

    @Operation(tags = {
            "URL Redirection" }, summary = "Redirect to original URL", description = "Redirects to the original URL and records click information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirecting to the original URL"),
            @ApiResponse(responseCode = "404", description = "Short code not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");

        String browser = userAgentService.getBrowser(userAgent);
        String operatingSystem = userAgentService.getOperatingSystem(userAgent);
        String deviceType = userAgentService.getDeviceType(userAgent);
        String referrer = request.getHeader("Referer");

        UrlMapping mapping = service.trackClick(
                shortCode,
                browser,
                operatingSystem,
                deviceType,
                referrer);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(mapping.getOriginalUrl()))
                .build();
    }
}
