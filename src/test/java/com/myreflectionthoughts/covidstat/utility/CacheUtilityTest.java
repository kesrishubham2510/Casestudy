package com.myreflectionthoughts.covidstat.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myreflectionthoughts.covidstat.contract.ICache;
import com.myreflectionthoughts.covidstat.entity.Trends;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheUtilityTest {

    private ICache<String, String> cacheService;
    private MappingUtility mappingUtility;

    @BeforeEach
    void setUp() {
        cacheService = mock(ICache.class);
        mappingUtility = mock(MappingUtility.class);
    }

    @Test
    void testGetKeyForRawAPIResponseForCurrentStat() {
        String result = CacheUtility.getKeyForRawAPIResponseForCurrentStat("india");
        assertEquals("india_currentstat", result);
    }

    @Test
    void testGetKeyForCountryVaccineCoverageTrends() {
        String result = CacheUtility.getKeyForCountryVaccineCoverageTrends("india", "2024-01-01");
        assertEquals("india_coverage_trend_2024-01-01", result);
    }

    @Test
    void testGetKeyForGlobalVaccineCoverageTrends() {
        String result = CacheUtility.getKeyForGlobalVaccineCoverageTrends("2024-01-01");
        assertEquals("global_coverage_trend_2024-01-01", result);
    }

    @Test
    void testGetKeyForAlertMessage() {
        String result = CacheUtility.getKeyForAlertMessage("india", "2024-01-01");
        assertEquals("alert_message_india_2024-01-01", result);
    }

    @Test
    void testCalculateTTLTimestamp_WithMinutes() {
        long ttl = CacheUtility.calculateTTLTimestamp(10);
        long nowPlus10 = ZonedDateTime.now().plusMinutes(10).toInstant().getEpochSecond();

        assertTrue(ttl >= nowPlus10 - 5 && ttl <= nowPlus10 + 5);
    }

    @Test
    void testCalculateTTLTimestamp_Midnight() {
        long ttl = CacheUtility.calculateTTLTimestamp(-1);

        ZonedDateTime midnightTomorrow = ZonedDateTime.now()
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay(ZonedDateTime.now().getZone());

        long expected = midnightTomorrow.toInstant().getEpochSecond();

        assertEquals(expected, ttl);
    }

    @Test
    void testGetTrendsFromCache_Success() throws JsonProcessingException {

        String country = "india";
        String date = "2024-01-01";

        String countryKey = CacheUtility.getKeyForCountryVaccineCoverageTrends(country, date);
        String globalKey = CacheUtility.getKeyForGlobalVaccineCoverageTrends(date);

        when(cacheService.get(countryKey)).thenReturn("{countryJson}");
        when(cacheService.get(globalKey)).thenReturn("{globalJson}");

        Trends countryTrends = new Trends();
        Trends globalTrends = new Trends();

        when(mappingUtility.parseToPOJO("{countryJson}", Trends.class)).thenReturn(countryTrends);
        when(mappingUtility.parseToPOJO("{globalJson}", Trends.class)).thenReturn(globalTrends);

        Map<String, Trends> result = CacheUtility.getTrendsFromCache(
                cacheService,
                mappingUtility,
                country,
                date
        );

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(countryTrends, result.get("india"));
        assertEquals(globalTrends, result.get("global"));
    }

    @Test
    void testGetTrendsFromCache_CountryMissing_ReturnsNull() {

        String country = "india";
        String date = "2024-01-01";

        String countryKey = CacheUtility.getKeyForCountryVaccineCoverageTrends(country, date);
        when(cacheService.get(countryKey)).thenReturn(null);

        Map<String, Trends> result = CacheUtility.getTrendsFromCache(
                cacheService,
                mappingUtility,
                country,
                date
        );

        assertNull(result);
    }

    @Test
    void testGetTrendsFromCache_GlobalMissing_ReturnsNull() throws JsonProcessingException {

        String country = "india";
        String date = "2024-01-01";

        String countryKey = CacheUtility.getKeyForCountryVaccineCoverageTrends(country, date);
        String globalKey = CacheUtility.getKeyForGlobalVaccineCoverageTrends(date);

        when(cacheService.get(countryKey)).thenReturn("{countryJson}");
        when(cacheService.get(globalKey)).thenReturn(null);

        when(mappingUtility.parseToPOJO("{countryJson}", Trends.class))
                .thenReturn(new Trends());

        Map<String, Trends> result = CacheUtility.getTrendsFromCache(
                cacheService,
                mappingUtility,
                country,
                date
        );

        assertNull(result);
    }

    @Test
    void testGetTrendsFromCache_JsonParsingException_ReturnsNull() throws JsonProcessingException {

        String country = "india";
        String date = "2024-01-01";

        String countryKey = CacheUtility.getKeyForCountryVaccineCoverageTrends(country, date);
        String globalKey = CacheUtility.getKeyForGlobalVaccineCoverageTrends(date);

        when(cacheService.get(countryKey)).thenReturn("{invalidJson}");
        when(cacheService.get(globalKey)).thenReturn("{globalJson}");

        when(mappingUtility.parseToPOJO("{invalidJson}", Trends.class))
                .thenThrow(JsonProcessingException.class);

        Map<String, Trends> result = CacheUtility.getTrendsFromCache(
                cacheService,
                mappingUtility,
                country,
                date
        );

        assertNull(result);
    }

    @Test
    void testGetLastTwoDayAlertFromCache() {
        String country = "india";
        String date = "2024-01-01";

        String expected = "alert message";

        when(cacheService.get("alert_message_india_2024-01-01"))
                .thenReturn(expected);

        String result = CacheUtility.getLastTwoDayAlertFromCache(cacheService, country, date);

        assertEquals(expected, result);
    }

    @Test
    void testGetLatestStatFromCache() {
        String country = "india";
        String expected = "latest data";

        when(cacheService.get("india_currentstat"))
                .thenReturn(expected);

        String result = CacheUtility.getLatestStatFromCache(cacheService, country);

        assertEquals(expected, result);
    }
}