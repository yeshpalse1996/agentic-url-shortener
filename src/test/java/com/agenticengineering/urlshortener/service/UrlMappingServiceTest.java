
package com.agenticengineering.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.agenticengineering.urlshortener.entity.AppUser;
import com.agenticengineering.urlshortener.entity.UrlClick;
import com.agenticengineering.urlshortener.entity.UrlMapping;
import com.agenticengineering.urlshortener.exception.UrlNotFoundException;
import com.agenticengineering.urlshortener.repository.AppUserRepository;
import com.agenticengineering.urlshortener.repository.UrlClickRepository;
import com.agenticengineering.urlshortener.repository.UrlMappingRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlMappingServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private UrlClickRepository urlClickRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private UrlMappingService urlMappingService;

    @Test
    void shouldCreateShortUrlSuccessfully() {

        AppUser owner = new AppUser();
        owner.setUsername("testuser");
        owner.setRole("USER");

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(owner));

        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl("https://google.com");

        when(urlMappingRepository.findByShortCode(anyString()))
                .thenReturn(Optional.empty());

        when(urlMappingRepository.save(any(UrlMapping.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UrlMapping result =
                urlMappingService.saveUrlMapping(mapping, "testuser");

        assertNotNull(result.getShortCode());
        assertEquals(7, result.getShortCode().length());
        assertEquals("https://google.com", result.getOriginalUrl());
        assertEquals(0, result.getClickCount());
        assertSame(owner, result.getOwner());

        verify(appUserRepository).findByUsername("testuser");
        verify(urlMappingRepository).save(mapping);
    }

    @Test
    void shouldThrowExceptionWhenOwnerDoesNotExist() {

        when(appUserRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl("https://google.com");

        assertThrows(
                IllegalArgumentException.class,
                () -> urlMappingService.saveUrlMapping(mapping, "unknown"));

        verify(urlMappingRepository, never()).save(any(UrlMapping.class));
    }

    @Test
    void shouldIncrementClickCountAndSaveClickEvent() {

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode("TCjYQht");
        mapping.setOriginalUrl("https://google.com");

        when(urlMappingRepository.findByShortCode("TCjYQht"))
                .thenReturn(Optional.of(mapping));

        when(urlMappingRepository.save(any(UrlMapping.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UrlMapping result = urlMappingService.trackClick(
                "TCjYQht",
                "Chrome",
                "Windows",
                "Desktop",
                null);

        assertEquals(1, result.getClickCount());

        ArgumentCaptor<UrlClick> captor =
                ArgumentCaptor.forClass(UrlClick.class);

        verify(urlClickRepository).save(captor.capture());

        UrlClick savedClick = captor.getValue();

        assertEquals("TCjYQht", savedClick.getShortCode());
        assertNotNull(savedClick.getClickedAt());

        verify(urlMappingRepository).save(mapping);
    }

    @Test
    void shouldSaveBrowserAndReferrerDetails() {

        UrlMapping mapping = new UrlMapping();
        mapping.setShortCode("TCjYQht");
        mapping.setOriginalUrl("https://google.com");

        when(urlMappingRepository.findByShortCode("TCjYQht"))
                .thenReturn(Optional.of(mapping));

        when(urlMappingRepository.save(any(UrlMapping.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        urlMappingService.trackClick(
                "TCjYQht",
                "Chrome",
                "Windows",
                "Desktop",
                "https://example.com");

        ArgumentCaptor<UrlClick> captor =
                ArgumentCaptor.forClass(UrlClick.class);

        verify(urlClickRepository).save(captor.capture());

        UrlClick savedClick = captor.getValue();

        assertEquals("Chrome", savedClick.getBrowser());
        assertEquals("Windows", savedClick.getOperatingSystem());
        assertEquals("Desktop", savedClick.getDeviceType());
        assertEquals("https://example.com", savedClick.getReferrer());
    }

    @Test
    void shouldThrowExceptionForUnknownShortCode() {

        when(urlMappingRepository.findByShortCode("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                UrlNotFoundException.class,
                () -> urlMappingService.trackClick("missing"));
    }
}