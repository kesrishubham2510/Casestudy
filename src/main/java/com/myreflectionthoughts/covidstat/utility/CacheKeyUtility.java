package com.myreflectionthoughts.covidstat.utility;

import java.time.LocalDate;

public class CacheKeyUtility {

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

    public static String getKeyForRawAPIResponseForCountryVaccineCoverage(String country, LocalDate referencedDate) {
        return String.format("%s_coverage_%s", country, referencedDate.toString());
    }

    public static String getKeyForRawAPIResponseForGlobalVaccineCoverage(LocalDate referencedDate) {
        return String.format("global_coverage_%s", referencedDate.toString());
    }

    public static String getKeyForComputedAPI(String country, LocalDate referencedDate) {
        return String.format("%s_%s",country,referencedDate.toString());
    }
}
