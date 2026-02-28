package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.config.CacheConfig;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisCacheServiceTest {

    @Test
    void testConstructorAndPing_Success() {

        CacheConfig cacheConfig = mock(CacheConfig.class);
        when(cacheConfig.getHost()).thenReturn("localhost");
        when(cacheConfig.getPort()).thenReturn(6379);

        try (MockedConstruction<Jedis> mocked = mockConstruction(Jedis.class,
                                                                 (mock, context) -> when(mock.ping()).thenReturn("PONG"))) {

            RedisCacheService service = new RedisCacheService(cacheConfig);

            assertNotNull(service);
            verify(mocked.constructed().get(0), times(2)).ping();
        }
    }

    @Test
    void testPing_Failure_ShouldThrowException() {

        CacheConfig cacheConfig = mock(CacheConfig.class);
        when(cacheConfig.getHost()).thenReturn("localhost");
        when(cacheConfig.getPort()).thenReturn(6379);

        try (MockedConstruction<Jedis> mocked = mockConstruction(Jedis.class,
                                                                 (mock, context) -> when(mock.ping()).thenReturn("FAIL"))) {

            assertThrows(CaseStudyException.class,
                         () -> new RedisCacheService(cacheConfig));
        }
    }

    @Test
    void testPut_ShouldCallRedisSetWithExpiry() {

        CacheConfig cacheConfig = mock(CacheConfig.class);
        when(cacheConfig.getHost()).thenReturn("localhost");
        when(cacheConfig.getPort()).thenReturn(6379);

        try (MockedConstruction<Jedis> mocked = mockConstruction(Jedis.class,
                                                                 (mock, context) -> when(mock.ping()).thenReturn("PONG"))) {

            RedisCacheService service = new RedisCacheService(cacheConfig);

            Jedis jedisMock = mocked.constructed().get(0);

            service.put("key1", "value1", 12345L);

            verify(jedisMock).set(eq("key1"), eq("value1"), any(SetParams.class));
        }
    }

    @Test
    void testGet_ShouldReturnValueFromRedis() {

        CacheConfig cacheConfig = mock(CacheConfig.class);
        when(cacheConfig.getHost()).thenReturn("localhost");
        when(cacheConfig.getPort()).thenReturn(6379);

        try (MockedConstruction<Jedis> mocked = mockConstruction(Jedis.class,
                                                                 (mock, context) -> {
                                                                     when(mock.ping()).thenReturn("PONG");
                                                                     when(mock.get("key1")).thenReturn("cachedValue");
                                                                 })) {

            RedisCacheService service = new RedisCacheService(cacheConfig);

            String result = service.get("key1");

            assertEquals("cachedValue", result);
        }
    }
}