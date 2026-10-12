package com.agenticengineering.urlshortener.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agenticengineering.urlshortener.entity.UrlMapping;
import com.agenticengineering.urlshortener.service.UrlMappingService;
import com.agenticengineering.urlshortener.service.UserAgentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UrlMappingController.class)
class UrlMappingControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlMappingService service;

    @MockitoBean
    private UserAgentService userAgentService;

    @Test
    void shouldCreateShortUrlThroughHttp() throws Exception {

        when(service.saveUrlMapping(
                any(UrlMapping.class), eq("testuser")))
                .thenAnswer(invocation -> {
                    UrlMapping mapping = invocation.getArgument(0);
                    mapping.setShortCode("TCjYQht");
                    return mapping;
                });

        mockMvc.perform(post("/api/urls")
                .with(jwt().jwt(jwt ->
                        jwt.subject("testuser")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "originalUrl": "https://google.com"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("TCjYQht"))
                .andExpect(jsonPath("$.originalUrl")
                        .value("https://google.com"));
    }

    @Test
    void shouldRejectInvalidUrlThroughHttp() throws Exception {

        mockMvc.perform(post("/api/urls")
                .with(jwt().jwt(jwt ->
                        jwt.subject("testuser")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "originalUrl": "google.com"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectBlankUrlThroughHttp() throws Exception {

        mockMvc.perform(post("/api/urls")
                .with(jwt().jwt(jwt ->
                        jwt.subject("testuser")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "originalUrl": ""
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRequireAuthenticationToCreateShortUrl()
            throws Exception {

        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "originalUrl": "https://google.com"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRedirectThroughHttp() throws Exception {

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode("TCjYQht");
        mapping.setOriginalUrl("https://google.com");

        when(userAgentService.getBrowser(any()))
                .thenReturn("Chrome");
        when(userAgentService.getOperatingSystem(any()))
                .thenReturn("Windows");
        when(userAgentService.getDeviceType(any()))
                .thenReturn("Desktop");

        when(service.trackClick(
                eq("TCjYQht"),
                eq("Chrome"),
                eq("Windows"),
                eq("Desktop"),
                eq("https://example.com")))
                .thenReturn(mapping);

        mockMvc.perform(get("/api/urls/TCjYQht")
                .header("User-Agent", "test-user-agent")
                .header("Referer", "https://example.com"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        "Location", "https://google.com"));
    }
}