package com.myreflectionthoughts.covidstat.service.impl.cacheservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myreflectionthoughts.covidstat.config.CacheTTLConfig;
import com.myreflectionthoughts.covidstat.contract.ICacheFacade;
import com.myreflectionthoughts.covidstat.entity.Trends;
import com.myreflectionthoughts.covidstat.utility.CacheUtility;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.logging.Logger;

@Component
public class TrendCacheService implements ICacheFacade<String, HashMap<String, Trends>> {

    private final CacheTTLConfig cacheTTLConfig;
    private final RedisCacheService redisCacheService;
    private final MappingUtility mappingUtility;
    private static final Logger logger = Logger.getLogger(CacheUtility.class.getSimpleName());

    public TrendCacheService(CacheTTLConfig cacheTTLConfig, RedisCacheService redisCacheService) {
        this.cacheTTLConfig = cacheTTLConfig;
        this.mappingUtility = MappingUtility.getMappingUtilityInstance();
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void put(HashMap<String, Trends> trendsHashMap) {

        trendsHashMap.keySet().stream().forEach(key->{

            Trends trends = trendsHashMap.get(key);
            String country = trends.getTrends().get(0).getCountry();
            String referencedDate = trends.getTrends().get(0).getReferencedDate();

            String cacheKeyForData = CacheUtility.getKeyForCountryVaccineCoverageTrends(country, referencedDate);
            redisCacheService.put(cacheKeyForData, MappingUtility.convertToJsonStructure(trends), CacheUtility.calculateTTLTimestamp(cacheTTLConfig.getVaccineCoverageTrends()));
            logger.info("Cache | Key:- "+cacheKeyForData+" cached to redis server successfully");
        });

    }

    @Override
    public HashMap<String, Trends> get(String key) {
        HashMap<String, Trends> trendsMap = null;

        try {
           String cachedData = redisCacheService.get(key);
           trendsMap.put(getEntryKey(key), mappingUtility.parseToPOJO(cachedData, Trends.class));
           logger.info("Cache | Key:- "+key+" retrieved successfully from redis server");
        } catch (JsonProcessingException e) {
            // TODO:- Log the exception
            logger.severe("Exception occured while retrieving the Key:- "+key+" from redis server");
        }
        
        return trendsMap;
    }
    
    private String getEntryKey(String cacheKey) {
        
        if (cacheKey.contains("global_coverage_trend")) {
            return "global";
        }

        return cacheKey.split("_coverage_trend_")[0];
    }
}
