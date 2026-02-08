package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.contract.ICache;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;

import java.util.logging.Logger;


// For the current use-case, I'm using String as key
public class RedisCacheService implements ICache<String, ResponseWrapper> {

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
    public void put(String key, ResponseWrapper value, long expiryTimestamp) {

    }

    @Override
    public ResponseWrapper get(String key) {
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
