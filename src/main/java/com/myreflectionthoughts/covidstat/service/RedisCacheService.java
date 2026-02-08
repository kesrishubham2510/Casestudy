package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.contract.ICache;

import java.util.logging.Logger;

public class RedisCacheService implements ICache {

    private String serverURL;
    private String hostname;
    private final Logger logger;

    private RedisCacheService(){
        logger = Logger.getLogger(RedisCacheService.class.getSimpleName());
        init();
        ping();
    }

    @Override
    public void init() {
        // Logic to initialise the connection properties
    }

    @Override
    public void ping() {
        // Logic to connect to the cache using the connection properties
    }

    @Override
    public <K, V> void put(K key, V value, long expiryTimestamp) {
        // logic to insert the value in the cache
    }

    @Override
    public <K, V> V get(K key) {
        return null;
    }

    // Singleton pattern
    private static class ICacheInstanceManager{
        private static final RedisCacheService cacheInstance = new RedisCacheService();
    }

    public static RedisCacheService getRedisCacheInstance(){
        return ICacheInstanceManager.cacheInstance;
    }
}
