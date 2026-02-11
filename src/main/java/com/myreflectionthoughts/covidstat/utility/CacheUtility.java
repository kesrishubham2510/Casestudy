package com.myreflectionthoughts.covidstat.utility;

import java.time.ZonedDateTime;

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
}
