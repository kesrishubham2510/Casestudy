package com.myreflectionthoughts.covidstat.service.impl.cacheservice;

import com.myreflectionthoughts.covidstat.config.CacheTTLConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisAlertMessageCacheServiceTest {

    private final CacheTTLConfig cacheTTLConfig;
    private final RedisCacheService redisCacheService;
    private final RedisAlertMessageCacheService cacheService;

    public RedisAlertMessageCacheServiceTest() {
        cacheTTLConfig = Mockito.mock(CacheTTLConfig.class);
        redisCacheService = Mockito.mock(RedisCacheService.class);

        cacheService = new RedisAlertMessageCacheService(cacheTTLConfig, redisCacheService);
    }

    @Test
    void testPut_success() {

        String message = "alertKey|This is alert";

        when(cacheTTLConfig.getAlertMessage()).thenReturn(1);
        doNothing().when(redisCacheService)
                .put(anyString(), anyString(), anyLong());

        cacheService.put(message);

        verify(redisCacheService, times(1))
                .put(eq("alertKey"), eq("This is alert"), anyLong());
    }

    @Test
    void testPut_invalidMessageFormat() {

        String invalidMessage = "invalidMessageWithoutDelimiter";

        when(cacheTTLConfig.getAlertMessage()).thenReturn(1);

        assertThrows(ArrayIndexOutOfBoundsException.class,
                     () -> cacheService.put(invalidMessage));
    }


    @Test
    void testGet_success() {

        when(redisCacheService.get("alertKey"))
                .thenReturn("This is alert");

        String result = cacheService.get("alertKey");

        assertEquals("This is alert", result);
        verify(redisCacheService, times(1)).get("alertKey");
    }
}