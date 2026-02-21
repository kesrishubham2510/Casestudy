package com.myreflectionthoughts.covidstat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myreflectionthoughts.covidstat.config.CacheTTLConfig;
import com.myreflectionthoughts.covidstat.config.CountryConfig;
import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
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
import com.myreflectionthoughts.covidstat.exception.CountryNotFoundException;
import com.myreflectionthoughts.covidstat.utility.CacheUtility;
import com.myreflectionthoughts.covidstat.utility.DataUtility;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;


@Service
public class Orchestrator {

    private final ITrendEvaluation<ExternalAPIResponse, ResponseWrapper> trendEvaluation;
    private final IDataSource<ResponseWrapper> remoteDataSource;
    private final Map<String, IExceptionHandler<CaseStudyException, Void>> exceptionHandlers;
    private final ICache<String, String> cacheService;
    private static final int MAX_DAY_TREND = 14;

    private final MappingUtility mappingUtility;
    private final CacheTTLConfig cacheTTLConfig;
    private final Logger logger;
    private final CountryConfig countryConfig;

    public Orchestrator(IDataSource<ResponseWrapper> remoteDataSource,
                        ICache<String, String> cacheService,
                        CacheTTLConfig cacheTTLConfig,
                        CountryConfig countryConfig,
                        // Will have to do injection using beanName here
                        @Qualifier(value = "badRequestExceptionHandler")
                        IExceptionHandler<CaseStudyException, Void> badRequesIExceptionHandler,
                        @Qualifier(value = "connectionExceptionHandler")
                        IExceptionHandler<CaseStudyException, Void> connectionExceptionHandler,
                        @Qualifier(value = "dataProcessingExceptionHandler")
                        IExceptionHandler<CaseStudyException, Void> genericExceptionHandler,
                        @Qualifier(value = "genericExceptionHandler")
                        IExceptionHandler<CaseStudyException, Void> dataProcessingExceptionHandler){

        this.trendEvaluation = NDayAverage.getNDayAverageInstance();
        this.remoteDataSource = remoteDataSource;
        this.cacheService = cacheService;
        this.mappingUtility = MappingUtility.getMappingUtilityInstance();
        this.exceptionHandlers = new HashMap<>();
        this.exceptionHandlers.put(ServiceConstant._ERR_OCCURRED_KEY, genericExceptionHandler);
        this.exceptionHandlers.put(ServiceConstant._ERR_PARSING_ERROR_LATEST_STAT_KEY, dataProcessingExceptionHandler);
        this.exceptionHandlers.put(ServiceConstant._ERR_BAD_REQUEST_KEY, badRequesIExceptionHandler);
        this.exceptionHandlers.put(ServiceConstant._ERR_REQUEST_PROCESSING_ERROR_KEY, connectionExceptionHandler);
        this.exceptionHandlers.put(ServiceConstant._ERR_PARSING_ERROR_VACCINE_COVERAGE_KEY, dataProcessingExceptionHandler);
        this.exceptionHandlers.put(ServiceConstant._ERR_CONNECT_KEY, connectionExceptionHandler);
        this.exceptionHandlers.put(ServiceConstant._ERR_PARSING_ERROR_DAILY_STAT_KEY, badRequesIExceptionHandler);
        this.cacheTTLConfig = cacheTTLConfig;
        this.countryConfig = countryConfig;
        this.logger = Logger.getLogger(Orchestrator.class.getSimpleName());
    }

    public List<CovidStatResponse> fetchComparisionStats(String[] countries, String referenceDate){
        List<CovidStatResponse> statResponses = new ArrayList<>();
        CovidStatResponse response = null;

        for(String country : countries) {

            validateInputCountry(country);

            if(StringUtils.isNotEmpty(country)) {
                response = fetchStats(country, referenceDate);
                statResponses.add(response);
            }
        }

        return statResponses;
    }

    // TODO: Make it handle error and graceful degradation
    public CovidStatResponse fetchStats(String country, String referencedDate){

        CovidStatResponse covidStatResponse = new CovidStatResponse();

        validateInputCountry(country);

        if(StringUtils.isEmpty(referencedDate)){
            logger.info("ReferencedDate is empty/null");
            referencedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }

        long daysBack = calculateTheDaysBack(referencedDate);

        try {

            String responseForLatestDataFromCache = CacheUtility.getLatestStatFromCache(cacheService, country);

            if (StringUtils.isNotBlank(responseForLatestDataFromCache)) {

                logger.info("Found latest data in the cache");

                String cacheKeyForResponse = CacheUtility.getKeyForComputedAPI(country, referencedDate);
                String responseFromCache = cacheService.get(cacheKeyForResponse);

                if (StringUtils.isNotBlank(responseFromCache)) {
                    try {

                        logger.info("Pre-computed response for country:- "+cacheKeyForResponse+", retrieved successfully");

                        covidStatResponse = mappingUtility.parseToPOJO(responseFromCache, CovidStatResponse.class);
                        covidStatResponse.setServerFromCache(true);
                        return covidStatResponse;
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
            cacheService.put(CacheUtility.getKeyForRawAPIResponseForCurrentStat(country), MappingUtility.convertToJsonStructure(latestStats), CacheUtility.calculateTTLTimestamp(cacheTTLConfig.getLatestStatCountry()));

            populateTrends(covidStatResponse, country, referencedDate, daysBack);
            populateAlertMessage(covidStatResponse, country, referencedDate, latestStats);

            covidStatResponse.setCountry(country);
            covidStatResponse.setActiveAsToday(String.valueOf(latestStats.getActive()));
            covidStatResponse.setNoOfCases(String.valueOf(latestStats.getCases()));
            covidStatResponse.setNoOfRecoveries(String.valueOf(latestStats.getRecovered()));

            cacheService.put(CacheUtility.getKeyForComputedAPI(country, referencedDate), MappingUtility.convertToJsonStructure(covidStatResponse), CacheUtility.calculateTTLTimestamp(cacheTTLConfig.getLatestStatCountry()));

        }catch (CaseStudyException exception){
            logger.severe("Exception occurred | returning default static response");
            exceptionHandlers.get(exception.getKey()).handleException(exception);
            return getDefaultResponse(country);
        }

        logger.info("Computed vaccine coverage trends for country:- "+country+", referencedDate:- "+referencedDate);
        return covidStatResponse;
    }


    private long calculateTheDaysBack(String referencedDate){

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
    
    private CovidStatResponse getDefaultResponse(String country){
        CovidStatResponse covidStatResponse = new CovidStatResponse();
        String staticResponse = DataUtility.getFileContent("data/StaticCovidStatResponse.json");
        covidStatResponse = DataUtility.convertTOPOJO(staticResponse, CovidStatResponse.class);
        covidStatResponse.setServerFromCache(true);
        return covidStatResponse;
    }

    private void populateTrends(CovidStatResponse covidStatResponse, String country, String referencedDate, long daysBack){
        Map<String, Trends> trendsMap = CacheUtility.getTrendsFromCache(cacheService, mappingUtility, country, referencedDate);

        if (Objects.isNull(trendsMap)) {

            ExternalAPIResponse countryVaccinationCoverage = null;
            ExternalAPIResponse globalVaccinationCoverage = null;

            // get the vaccine coverage for country
            countryVaccinationCoverage = (ExternalAPIResponse) remoteDataSource.getVaccineCoverage(country, daysBack + MAX_DAY_TREND);
            covidStatResponse.setDosesAdministeredInCountry(countryVaccinationCoverage.getTimeline().get(countryVaccinationCoverage.getTimeline().size()-1).getTotal());

            // get the vaccine coverage for global level
            globalVaccinationCoverage = (ExternalAPIResponse) remoteDataSource.getVaccineCoverage("global", daysBack + MAX_DAY_TREND);
            covidStatResponse.setDosesAdministeredGlobally(globalVaccinationCoverage.getTimeline().get(countryVaccinationCoverage.getTimeline().size()-1).getTotal());


            Trends countryTrends = (Trends) trendEvaluation.calculate(countryVaccinationCoverage, new int[]{7, 14});
            Trends globalTrends = (Trends) trendEvaluation.calculate(globalVaccinationCoverage, new int[]{7, 14});

            trendsMap = new HashMap<>();

            trendsMap.put(country, countryTrends);
            trendsMap.put("global", globalTrends);

            // Keeping the TTL as 120 hours because, a referenced historical trend will never change
            cacheService.put(CacheUtility.getKeyForCountryVaccineCoverageTrends(country, referencedDate), MappingUtility.convertToJsonStructure(countryTrends), CacheUtility.calculateTTLTimestamp(cacheTTLConfig.getVaccineCoverageTrends()));
            cacheService.put(CacheUtility.getKeyForGlobalVaccineCoverageTrends(referencedDate), MappingUtility.convertToJsonStructure(globalTrends), CacheUtility.calculateTTLTimestamp(cacheTTLConfig.getVaccineCoverageTrends()));
        }

        covidStatResponse.setTrends(trendsMap);
    }

    private void populateAlertMessage(CovidStatResponse covidStatResponse, String country, String referencedDate, ExternalAPIResponse latestStats ){
        String alertMessage = CacheUtility.getLastTwoDayAlertFromCache(cacheService, country, referencedDate);

        if (Objects.isNull(alertMessage)) {

            logger.info("Pre-computed alert message not found:- "+country+", referencedDate:- "+referencedDate);

            LastTwoDaysResponse lastTwoDaysResponse = (LastTwoDaysResponse) remoteDataSource.getDataForAlerts(country, 0L);

            lastTwoDaysResponse.getLastTwoDaysResponse().add(latestStats);
            alertMessage = evaluateAlertMessage(lastTwoDaysResponse);

            cacheService.put(CacheUtility.getKeyForAlertMessage(country, referencedDate), alertMessage, CacheUtility.calculateTTLTimestamp(cacheTTLConfig.getAlertMessage()));
        }
    }

    private void validateInputCountry(String country){
        if(!countryConfig.getSupportedCountries().contains(country)){
            throw new CountryNotFoundException(String.format("%s, is not supported", country));
        }
    }
}
