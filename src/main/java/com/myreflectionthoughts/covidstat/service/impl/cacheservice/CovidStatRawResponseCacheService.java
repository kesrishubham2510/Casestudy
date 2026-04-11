package com.myreflectionthoughts.covidstat.service.impl.cacheservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myreflectionthoughts.covidstat.config.CacheTTLConfig;
import com.myreflectionthoughts.covidstat.contract.ICacheFacade;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.utility.CacheUtility;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.logging.Logger;

public class CovidStatRawResponseCacheService implements ICacheFacade<String, ExternalAPIResponse> {

    private final CacheTTLConfig cacheTTLConfig;
    private final RedisCacheService redisCacheService;
    private final MappingUtility mappingUtility;
    private static final Logger logger = Logger.getLogger(CovidStatRawResponseCacheService.class.getSimpleName());


    public CovidStatRawResponseCacheService(CacheTTLConfig cacheTTLConfig, RedisCacheService redisCacheService) {
        this.cacheTTLConfig = cacheTTLConfig;
        this.mappingUtility = MappingUtility.getMappingUtilityInstance();
        this.redisCacheService = redisCacheService;
    }
    @Override
    public void put(ExternalAPIResponse externalAPIResponse) {
        String cacheKeyForData = CacheUtility.getKeyForRawAPIResponseForCurrentStat(externalAPIResponse.getCountry());

        redisCacheService.put(cacheKeyForData, MappingUtility.convertToJsonStructure(externalAPIResponse),
                              CacheUtility.calculateTTLTimestamp(cacheTTLConfig.getLatestStatCountry()));

        logger.info("Cache | Key:- "+cacheKeyForData+" cached to redis server successfully");
    }

    @Override
    public ExternalAPIResponse get(String key) {

        String cachedResponse = redisCacheService.get(key);

        logger.info("Cache | Key:- "+key+" retrieved successfully from redis server");
        try {
            if(StringUtils.isNotBlank(cachedResponse)) {
                return mappingUtility.parseToPOJO(cachedResponse, ExternalAPIResponse.class);
            }else{
                logger.severe("No value present in the cache | key:- "+key);
                return null;
            }
        } catch (JsonProcessingException e) {
            logger.severe("Exception occured while parsing cached response:- {}"+key);
            return null;
        }
    }
}
