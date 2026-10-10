
package com.agenticengineering.urlshortener.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agenticengineering.urlshortener.entity.UrlClick;
import com.agenticengineering.urlshortener.entity.UrlMapping;
import com.agenticengineering.urlshortener.exception.UrlNotFoundException;
import com.agenticengineering.urlshortener.repository.UrlClickRepository;
import com.agenticengineering.urlshortener.repository.UrlMappingRepository;

@Service
public class UrlMappingService {

    private static final Logger logger =
            LoggerFactory.getLogger(UrlMappingService.class);

    private static final String CHARACTERS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int CODE_LENGTH = 7;

    private final SecureRandom random = new SecureRandom();

    private final UrlMappingRepository urlMappingRepository;
    private final UrlClickRepository urlClickRepository;

    public UrlMappingService(
            UrlMappingRepository urlMappingRepository,
            UrlClickRepository urlClickRepository) {
        this.urlMappingRepository = urlMappingRepository;
        this.urlClickRepository = urlClickRepository;
    }

    public UrlMapping saveUrlMapping(UrlMapping mapping) {
        String shortCode;

        do {
            shortCode = generateShortCode();
        } while (urlMappingRepository.findByShortCode(shortCode).isPresent());

        mapping.setShortCode(shortCode);

        UrlMapping savedMapping = urlMappingRepository.save(mapping);

        logger.info(
                "Short URL created successfully. Code: {}",
                savedMapping.getShortCode());

        return savedMapping;
    }

    private String generateShortCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }

        return code.toString();
    }

    public UrlMapping getByShortCode(String shortCode) {
        return urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    logger.warn(
                            "Short URL not found. Code: {}",
                            shortCode);
                    return new UrlNotFoundException(shortCode);
                });
    }

    @Transactional
    public UrlMapping trackClick(String shortCode) {

        UrlMapping mapping = getByShortCode(shortCode);

        mapping.setClickCount(mapping.getClickCount() + 1);

        UrlClick click = new UrlClick(
                shortCode,
                LocalDateTime.now());

        urlClickRepository.save(click);

        logger.info(
                "Click tracked for short code: {}",
                shortCode);

        return urlMappingRepository.save(mapping);
    }

    @Transactional
    public UrlMapping trackClick(
            String shortCode,
            String browser,
            String operatingSystem,
            String deviceType,
            String referrer) {

        UrlMapping mapping = getByShortCode(shortCode);

        mapping.setClickCount(mapping.getClickCount() + 1);

        UrlClick click = new UrlClick(
                shortCode,
                LocalDateTime.now(),
                browser,
                operatingSystem,
                deviceType,
                referrer);

        urlClickRepository.save(click);

        logger.info(
                "Click tracked for short code: {}",
                shortCode);

        return urlMappingRepository.save(mapping);
    }
}