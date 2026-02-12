package com.myreflectionthoughts.covidstat.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myreflectionthoughts.covidstat.contract.ICache;
import com.myreflectionthoughts.covidstat.entity.Trends;
import com.myreflectionthoughts.covidstat.service.RedisCacheService;
import io.micrometer.common.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class CacheUtility {

    private static final String CURRENT_STAT_SUFFIX = "_currentstat";
    private static final String ONE_DAY_PREV_SUFFIX = "_oneday_prev";
    private static final String TWO_DAY_PREV_SUFFIX = "_twoday_prev";

    public static String getKeyForRawAPIResponseForCurrentStat(String country) {
        return country + CURRENT_STAT_SUFFIX;
    }

    public static String getKeyForRawAPIResponse_OneDayPrev(String country) {
        return country + ONE_DAY_PREV_SUFFIX;
    }

    public static String getKeyForRawAPIResponse_TwoDayPrev(String country) {
        return country + TWO_DAY_PREV_SUFFIX;
    }

    public static String getKeyForCountryVaccineCoverageTrends(String country, String referencedDate) {
        return String.format("%s_coverage_trend_%s", country, referencedDate);
    }

    public static String getKeyForGlobalVaccineCoverageTrends(String referencedDate) {
        return String.format("global_coverage_trend_%s", referencedDate);
    }

    public static String getKeyForComputedAPI(String country, String referencedDate) {
        return String.format("%s_%s",country,referencedDate);
    }

    public static String getKeyForAlertMessage(String country, String referencedDate) {
        return String.format("alert_message_%s_%s",country,referencedDate);
    }

    public static long calculateTTLTimestamp(int minutes){
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime midnightToday = now.toLocalDate().atStartOfDay(now.getZone());
        ZonedDateTime ttl = null;

        if(minutes==-1){
           ttl = midnightToday.plusDays(1);
        }else{
           ttl = now.plusMinutes(minutes);
        }

        return ttl.toInstant().getEpochSecond();
    }

    public static Map<String, Trends> getTrendsFromCache(ICache<String, String> cacheService, MappingUtility mappingUtility, String country, String referencedDate){

        Map<String, Trends> trendsMap = new HashMap<>();

        String cacheKeyForGlobalVaccineCoverageTrends = CacheUtility.getKeyForGlobalVaccineCoverageTrends(referencedDate);
        String responseForGlobalVaccineCoverageTrends = cacheService.get(cacheKeyForGlobalVaccineCoverageTrends);
        String cacheKeyForCountryVaccineCoverageTrends = CacheUtility.getKeyForCountryVaccineCoverageTrends(country, referencedDate);
        String responseForCountryVaccineCoverageTrends = cacheService.get(cacheKeyForCountryVaccineCoverageTrends);


        if(StringUtils.isNotBlank(responseForCountryVaccineCoverageTrends)){
            try {
                trendsMap.put(country, mappingUtility.parseToPOJO(responseForCountryVaccineCoverageTrends, Trends.class));
            } catch (JsonProcessingException e) {
                // remove the invalid trend from cache
                return null;
            }
        }else{
            return null;
        }

        if(StringUtils.isNotBlank(responseForGlobalVaccineCoverageTrends)){
            try {
                trendsMap.put("global", mappingUtility.parseToPOJO(responseForGlobalVaccineCoverageTrends, Trends.class));
            } catch (JsonProcessingException e) {
                // remove the invalid trend from cache
                return null;
            }
        }else{
            return null;
        }

        return trendsMap.size()==1 ? null : trendsMap;
    }

    public static String getLastTwoDayAlertFromCache(ICache<String, String> cacheService, String country, String referencedDate){
        String cacheKeyForAlertMessage = CacheUtility.getKeyForAlertMessage(country, referencedDate);
        return cacheService.get(cacheKeyForAlertMessage);
    }

    public static String getLatestStatFromCache(ICache<String, String> cacheService, String country){
        String cacheKeyForLatestData = CacheUtility.getKeyForRawAPIResponseForCurrentStat(country);
        return cacheService.get(cacheKeyForLatestData);
    }
}
