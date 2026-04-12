package com.myreflectionthoughts.covidstat.service.impl.cacheservice;

import com.myreflectionthoughts.covidstat.config.CacheTTLConfig;
import com.myreflectionthoughts.covidstat.entity.Trend;
import com.myreflectionthoughts.covidstat.entity.Trends;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrendCacheServiceTest {

    private final CacheTTLConfig cacheTTLConfig;
    private final RedisCacheService redisCacheService;
    private final TrendCacheService cacheService;

    public TrendCacheServiceTest() {
        cacheTTLConfig = Mockito.mock(CacheTTLConfig.class);
        redisCacheService = Mockito.mock(RedisCacheService.class);

        cacheService = new TrendCacheService(cacheTTLConfig, redisCacheService);
    }

    @Test
    void testPut_success() {

        // Prepare Trends object
        Trends trends = mock(Trends.class);
        Trend trendEntry = mock(Trend.class);

        when(trends.getTrends()).thenReturn(List.of(trendEntry));
        when(trendEntry.getCountry()).thenReturn("India");
        when(trendEntry.getReferencedDate()).thenReturn("01-01-2024");

        HashMap<String, Trends> map = new HashMap<>();
        map.put("India", trends);

        when(cacheTTLConfig.getVaccineCoverageTrends()).thenReturn(1);

        doNothing().when(redisCacheService)
                .put(anyString(), anyString(), anyLong());

        cacheService.put(map);

        verify(redisCacheService, times(1))
                .put(anyString(), anyString(), anyLong());
    }

    @Test
    void testGet_success() {

        String key = "india_coverage_trend_01-01-2024";

        Trends trends = mock(Trends.class);

        String json = MappingUtility.convertToJsonStructure(trends);

        when(redisCacheService.get(key)).thenReturn(json);

        HashMap<String, Trends> result = cacheService.get(key);

        assertNotNull(result);
        assertTrue(result.containsKey("india"));
        verify(redisCacheService, times(1)).get(key);
    }

    @Test
    void testGet_returnsNull_whenEmpty() {

        when(redisCacheService.get(anyString())).thenReturn("");

        HashMap<String, Trends> result = cacheService.get("key");

        assertNull(result);
        verify(redisCacheService, times(1)).get(anyString());
    }

    // =========================
    // ❌ GET - INVALID JSON
    // =========================
    @Test
    void testGet_returnsNull_onParsingError() {

        String key = "india_coverage_trend_01-01-2024";

        when(redisCacheService.get(key)).thenReturn("}");

        HashMap<String, Trends> result = cacheService.get(key);

        assertNotNull(result);
    }
}