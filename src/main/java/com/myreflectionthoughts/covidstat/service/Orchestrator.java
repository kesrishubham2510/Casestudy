package com.myreflectionthoughts.covidstat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myreflectionthoughts.covidstat.contract.ICache;
import com.myreflectionthoughts.covidstat.contract.ITrendEvaluation;
import com.myreflectionthoughts.covidstat.contract.IDataSource;
import com.myreflectionthoughts.covidstat.contract.IExceptionHandler;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.Trends;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.externaldto.LastTwoDaysResponse;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import com.myreflectionthoughts.covidstat.utility.CacheUtility;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;


@Service
public class Orchestrator {

    private final ITrendEvaluation<ExternalAPIResponse, ResponseWrapper> trendEvaluation;
    private final IDataSource<ResponseWrapper> remoteDataSource;
    private final Map<Integer, IExceptionHandler> exceptionHandlers;
    private final ICache<String, String> cacheService;
    private static final int MAX_DAY_TREND = 14;

    private final MappingUtility mappingUtility;
    private final Logger logger;

    public Orchestrator(ITrendEvaluation<ExternalAPIResponse, ResponseWrapper> trendEvaluation,
                        IDataSource<ResponseWrapper> remoteDataSource,
                        ICache<String, String> cacheService,
                        // Will have to do injection using beanName here
                        IExceptionHandler<String, Void> badRequesIExceptionHandler,
                        IExceptionHandler<String, Void> connectionExceptionHandler){

        this.trendEvaluation = trendEvaluation;
        this.remoteDataSource = remoteDataSource;
        this.cacheService = cacheService;
        this.mappingUtility = MappingUtility.getMappingUtilityInstance();
        this.exceptionHandlers = new HashMap<>();
        this.exceptionHandlers.put(4, badRequesIExceptionHandler);
        this.exceptionHandlers.put(5, connectionExceptionHandler);
        this.logger = Logger.getLogger(Orchestrator.class.getSimpleName());
    }

    // TODO: Make it handle error and graceful degradation
    public CovidStatResponse fetchStats(String country, String referencedDate){

        CovidStatResponse covidStatResponse = new CovidStatResponse();
        if(referencedDate==null){
            referencedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }

        // Happy Path

        /*
        *  Generate the cache key
        *  --> If entry exists, return the cached response
        *  --> fetch the data, calculate everything, cache it and then return
        *
        */
        long daysBack = calculateTheDaysBack(referencedDate);

        try {

            String responseForLatestDataFromCache = getLatestStatFromCache(country);

            if (StringUtils.isNotBlank(responseForLatestDataFromCache)) {

                String cacheKeyForResponse = CacheUtility.getKeyForComputedAPI(country, referencedDate);
                String responseFromCache = cacheService.get(cacheKeyForResponse);

                if (StringUtils.isNotBlank(responseFromCache)) {
                    try {
                        return mappingUtility.parseToPOJO(responseFromCache, CovidStatResponse.class);
                    } catch (JsonProcessingException e) {
                        // remove the invalidated response from the cache
                        logger.warning("The cached response for Key:- " + cacheKeyForResponse + ", has been deleted");
                    }
                }
            }

            ExternalAPIResponse latestStats = null;

            latestStats = (ExternalAPIResponse) remoteDataSource.getLatestStats(country, 0L);
            latestStats.setCountry(country);

            // Keeping TTL as 35 minutes, as every 30 minutes new data s pushed into the API
            // TODO: make it configurable
            cacheService.put(CacheUtility.getKeyForRawAPIResponseForCurrentStat(country), MappingUtility.convertToJsonStructure(latestStats), CacheUtility.calculateTTLTimestamp(35));

            Map<String, Trends> trendsMap = getTrendsFromCache(country, referencedDate);

            if (Objects.isNull(trendsMap)) {

                ExternalAPIResponse countryVaccinationCoverage = null;
                ExternalAPIResponse globalVaccinationCoverage = null;

                // get the vaccine coverage for country
                countryVaccinationCoverage = (ExternalAPIResponse) remoteDataSource.getVaccineCoverage(country, daysBack + MAX_DAY_TREND);

                // get the vaccine coverage for global level
                globalVaccinationCoverage = (ExternalAPIResponse) remoteDataSource.getVaccineCoverage(null, daysBack + MAX_DAY_TREND);

                Trends countryTrends = (Trends) trendEvaluation.calculate(countryVaccinationCoverage, new int[]{7, 14});
                Trends globalTrends = (Trends) trendEvaluation.calculate(globalVaccinationCoverage, new int[]{7, 14});

                trendsMap = new HashMap<>();

                trendsMap.put(country, countryTrends);
                trendsMap.put("global", globalTrends);

                // Keeping the TTL as 60 hours because, a referenced historical trend will never change
                // TODO: make it configurable
                cacheService.put(CacheUtility.getKeyForCountryVaccineCoverageTrends(country, referencedDate), MappingUtility.convertToJsonStructure(countryTrends), CacheUtility.calculateTTLTimestamp(60 * 60));
                cacheService.put(CacheUtility.getKeyForGlobalVaccineCoverageTrends(referencedDate), MappingUtility.convertToJsonStructure(globalTrends), CacheUtility.calculateTTLTimestamp(60 * 60));
            }

            // get the lastTwo days data
            String alertMessage = getLastTwoDayAlertFromCache(country, referencedDate);

            if (Objects.isNull(alertMessage)) {

                LastTwoDaysResponse lastTwoDaysResponse = (LastTwoDaysResponse) remoteDataSource.getDataForAlerts(country, 0L);

                lastTwoDaysResponse.getLastTwoDaysResponse().add(latestStats);
                alertMessage = evaluateAlertMessage(lastTwoDaysResponse);

                // TODO: make it configurable
                cacheService.put(CacheUtility.getKeyForAlertMessage(country, referencedDate), alertMessage, CacheUtility.calculateTTLTimestamp(35));
            }


            covidStatResponse.setCountry(country);
            covidStatResponse.setTrends(trendsMap);
            covidStatResponse.setActiveAsToday(String.valueOf(latestStats.getActive()));
            covidStatResponse.setNoOfCases(String.valueOf(latestStats.getCases()));
            covidStatResponse.setNoOfRecoveries(String.valueOf(latestStats.getRecovered()));
            covidStatResponse.setAlertMessage(alertMessage);

            // TODO: make it configurable
            cacheService.put(CacheUtility.getKeyForComputedAPI(country, referencedDate), MappingUtility.convertToJsonStructure(covidStatResponse), CacheUtility.calculateTTLTimestamp(35));

        }catch (CaseStudyException exception){
            return getDefaultResponse();
        }

        return covidStatResponse;
    }


    private long calculateTheDaysBack(String referencedDate){
        if(StringUtils.isEmpty(referencedDate))
            return 0L;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate givenDate = LocalDate.parse(referencedDate, formatter);
        LocalDate today = LocalDate.now();

        return ChronoUnit.DAYS.between(givenDate, today);
    }

    private String evaluateAlertMessage(LastTwoDaysResponse lastTwoDaysResponse){

        String alertMessage = "";
        List<ExternalAPIResponse> apiResponses = lastTwoDaysResponse.getLastTwoDaysResponse();

        double avgPrevious =
                (apiResponses.get(0).getTodayCases() + apiResponses.get(1).getTodayCases()) / 2.0;

        double percentIncrease =
                ((apiResponses.get(2).getTodayCases() - avgPrevious) / avgPrevious) * 100;


        if (percentIncrease > 20) {
            alertMessage = "Rising cases - Follow safety protocols";
        }

        return alertMessage;
    }

    private Map<String, Trends> getTrendsFromCache(String country, String referencedDate){

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

        return trendsMap.isEmpty() || trendsMap.size()==1 ? null : trendsMap;
    }

    private String getLastTwoDayAlertFromCache(String country, String referencedDate){
        String cacheKeyForAlertMessage = CacheUtility.getKeyForAlertMessage(country, referencedDate);
        return cacheService.get(cacheKeyForAlertMessage);
    }

    private String getLatestStatFromCache(String country){
        String cacheKeyForLatestData = CacheUtility.getKeyForRawAPIResponseForCurrentStat(country);
        return cacheService.get(cacheKeyForLatestData);
    }
    
    private CovidStatResponse getDefaultResponse(){
        // TODO:- Implement to return global default response
        return null;
    }
}
