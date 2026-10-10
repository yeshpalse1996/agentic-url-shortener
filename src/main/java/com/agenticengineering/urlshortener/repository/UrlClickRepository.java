package com.agenticengineering.urlshortener.repository;

import com.agenticengineering.urlshortener.entity.UrlClick;
import org.springframework.data.jpa.repository.JpaRepository;

import com.agenticengineering.urlshortener.dto.DailyClickAnalytics;
import com.agenticengineering.urlshortener.dto.ReferrerStat;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import com.agenticengineering.urlshortener.dto.ClickStat;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface UrlClickRepository
    extends JpaRepository<UrlClick, Long> {

  // counts click records.
  long countByShortCode(String shortCode);

  // gets the latest click.
  Optional<UrlClick> findTopByShortCodeOrderByClickedAtDesc(String shortCode);

  // returns click history, newest first.
  List<UrlClick> findByShortCodeOrderByClickedAtDesc(
      String shortCode);

  @Query(value = """
      SELECT DATE(clicked_at) AS date,
             COUNT(*) AS clicks
      FROM url_clicks
      WHERE short_code = :shortCode
      GROUP BY DATE(clicked_at)
      ORDER BY DATE(clicked_at)
      """, nativeQuery = true)
  List<Object[]> findDailyClicks(
      @Param("shortCode") String shortCode);

  @Query("""
      SELECT new com.agenticengineering.urlshortener.dto.ClickStat(
          uc.browser, COUNT(uc)
      )
      FROM UrlClick uc
      WHERE uc.shortCode = :shortCode
        AND uc.browser IS NOT NULL
      GROUP BY uc.browser
      ORDER BY COUNT(uc) DESC
      """)
  List<ClickStat> countClicksByBrowser(
      @Param("shortCode") String shortCode);

  @Query("""
      SELECT new com.agenticengineering.urlshortener.dto.ClickStat(
          uc.deviceType, COUNT(uc)
      )
      FROM UrlClick uc
      WHERE uc.shortCode = :shortCode
        AND uc.deviceType IS NOT NULL
      GROUP BY uc.deviceType
      ORDER BY COUNT(uc) DESC
      """)
  List<ClickStat> countClicksByDevice(
      @Param("shortCode") String shortCode);

  @Query("""
      SELECT new com.agenticengineering.urlshortener.dto.ClickStat(
          uc.operatingSystem, COUNT(uc)
      )
      FROM UrlClick uc
      WHERE uc.shortCode = :shortCode
        AND uc.operatingSystem IS NOT NULL
      GROUP BY uc.operatingSystem
      ORDER BY COUNT(uc) DESC
      """)
  List<ClickStat> countClicksByOperatingSystem(
      @Param("shortCode") String shortCode);

  @Query("""
      SELECT new com.agenticengineering.urlshortener.dto.ReferrerStat(
          uc.referrer, COUNT(uc)
      )
      FROM UrlClick uc
      WHERE uc.shortCode = :shortCode
        AND uc.referrer IS NOT NULL
        AND uc.referrer <> ''
      GROUP BY uc.referrer
      ORDER BY COUNT(uc) DESC
      """)
  List<ReferrerStat> countClicksByReferrer(
      @Param("shortCode") String shortCode);

}