package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.contract.ICache;
import com.myreflectionthoughts.covidstat.contract.ITrendEvaluation;
import com.myreflectionthoughts.covidstat.contract.IDataSource;
import com.myreflectionthoughts.covidstat.contract.IExceptionHandler;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.Trends;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.externaldto.LastTwoDaysResponse;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class Orchestrator {

    private final ITrendEvaluation<ExternalAPIResponse, ResponseWrapper> trendEvaluation;
    private final IDataSource<ResponseWrapper> remoteDataSource;
    private final Map<Integer, IExceptionHandler> exceptionHandlers;
    private final ICache<String, String> cacheService;
    private static final int MAX_DAY_TREND = 14;
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
        this.exceptionHandlers = new HashMap<>();
        this.exceptionHandlers.put(4, badRequesIExceptionHandler);
        this.exceptionHandlers.put(5, connectionExceptionHandler);
        this.logger = Logger.getLogger(Orchestrator.class.getSimpleName());
    }

    public CovidStatResponse fetchStats(String country, String referencedDate){

        CovidStatResponse covidStatResponse = new CovidStatResponse();
        // Happy Path

        /*
        *  Generate the cache key
        *  --> If entry exists, return the cached response
        *  --> fetch the data, calculate everything, cache it and then return
        *
        */

        long daysBack = calculateTheDaysBack(referencedDate);
        ExternalAPIResponse latestStats = (ExternalAPIResponse) remoteDataSource.getLatestStats(country, 0L);
        latestStats.setCountry(country);

        // get the vaccine coverage for country
        ExternalAPIResponse countryVaccinationCoverage = new ExternalAPIResponse();
        countryVaccinationCoverage.setTimeline(((ExternalAPIResponse)remoteDataSource.getVaccineCoverage(country, daysBack + MAX_DAY_TREND)).getTimeline());

        // get the vaccine coverage for global level
        ExternalAPIResponse globalVaccinationCoverage = new ExternalAPIResponse();
        globalVaccinationCoverage.setTimeline(((ExternalAPIResponse) remoteDataSource.getVaccineCoverage(null, daysBack + MAX_DAY_TREND)).getTimeline());

        // get the lastTwo days data
        LastTwoDaysResponse lastTwoDaysResponse = (LastTwoDaysResponse) remoteDataSource.getDataForAlerts(country, 0L);
        lastTwoDaysResponse.getLastTwoDaysResponse().add(latestStats);

        Map<String, Trends> trendsMap = new HashMap<>();

        Trends countryTrends = (Trends) trendEvaluation.calculate(countryVaccinationCoverage, new int[] {7, 14});
        Trends globalTrends = (Trends) trendEvaluation.calculate(globalVaccinationCoverage, new int[] {7, 14});

        trendsMap.put(country, countryTrends);
        trendsMap.put("global", globalTrends);


        covidStatResponse.setCountry(country);
        covidStatResponse.setTrends(trendsMap);
        covidStatResponse.setActiveAsToday(String.valueOf(latestStats.getActive()));
        covidStatResponse.setNoOfCases(String.valueOf(latestStats.getCases()));
        covidStatResponse.setNoOfRecoveries(String.valueOf(latestStats.getRecovered()));
        covidStatResponse.setAlertMessage(evaluateAlertMessage(lastTwoDaysResponse));

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
}
