package com.agenticengineering.urlshortener;

import com.agenticengineering.urlshortener.entity.UrlMapping;
import com.agenticengineering.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UrlMappingRepositoryIntegrationTest {

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Test
    void shouldSaveUrlMappingToDatabase() {
        UrlMapping mapping = new UrlMapping();

        mapping.setOriginalUrl("https://www.google.com");
        mapping.setShortCode("test123");

        UrlMapping saved = urlMappingRepository.save(mapping);

        assertNotNull(saved.getId());
        assertEquals("https://www.google.com", saved.getOriginalUrl());
        assertEquals("test123", saved.getShortCode());
    }
}