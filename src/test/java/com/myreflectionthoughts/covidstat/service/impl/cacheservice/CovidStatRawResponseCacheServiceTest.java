package com.myreflectionthoughts.covidstat.service.impl.cacheservice;

import com.myreflectionthoughts.covidstat.config.CacheTTLConfig;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import com.myreflectionthoughts.covidstat.utility.TestDataUtility;
import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CovidStatRawResponseCacheServiceTest {

    private final CacheTTLConfig cacheTTLConfig;
    private final RedisCacheService redisCacheService;

    private final CovidStatRawResponseCacheService cacheService;

    public CovidStatRawResponseCacheServiceTest(){
        cacheTTLConfig = Mockito.mock(CacheTTLConfig.class);
        redisCacheService = Mockito.mock(RedisCacheService.class);

        cacheService = new CovidStatRawResponseCacheService(cacheTTLConfig, redisCacheService);
    }

    @Test
    void testPut_success() {

        ExternalAPIResponse response = new ExternalAPIResponse();
        response.setCountry("India");

        when(cacheTTLConfig.getLatestStatCountry()).thenReturn(1);
        doNothing().when(redisCacheService).put(anyString(), anyString(), anyLong());

        cacheService.put(response);

        verify(redisCacheService, times(1)).put(anyString(), anyString(), anyLong());
    }

    @Test
    void testGet(){
        String cachedResponse = MappingUtility.convertToJsonStructure(TestDataUtility.getDefaultResponse("data/LatestCovidStat.json", ExternalAPIResponse.class));
        when(redisCacheService.get(anyString())).thenReturn(cachedResponse);
        assertNotNull(cacheService.get("key"));
        verify(redisCacheService, times(1)).get(anyString());
    }

    @Test
    void testGet_ReturnsNull(){
        when(redisCacheService.get(anyString())).thenReturn("/");
        assertNull(cacheService.get("key"));
        verify(redisCacheService, times(1)).get(anyString());
    }

}