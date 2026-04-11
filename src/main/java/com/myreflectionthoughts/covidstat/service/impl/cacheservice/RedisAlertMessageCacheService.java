package com.myreflectionthoughts.covidstat.service.impl.cacheservice;

import com.myreflectionthoughts.covidstat.config.CacheTTLConfig;
import com.myreflectionthoughts.covidstat.contract.ICacheFacade;
import com.myreflectionthoughts.covidstat.utility.CacheUtility;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

public class RedisAlertMessageCacheService implements ICacheFacade<String, String> {

    private final CacheTTLConfig cacheTTLConfig;
    private final RedisCacheService redisCacheService;
    private final MappingUtility mappingUtility;
    private static final Logger logger = Logger.getLogger(RedisAlertMessageCacheService.class.getSimpleName());

    public RedisAlertMessageCacheService(CacheTTLConfig cacheTTLConfig, RedisCacheService redisCacheService) {
        this.cacheTTLConfig = cacheTTLConfig;
        this.mappingUtility = MappingUtility.getMappingUtilityInstance();
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void put(String message) {
        String[] tokens = message.split("_");
        redisCacheService.put(tokens[0], tokens[1], CacheUtility.calculateTTLTimestamp(cacheTTLConfig.getAlertMessage()));
    }

    @Override
    public String get(String key) {
        return redisCacheService.get(key);
    }
}
