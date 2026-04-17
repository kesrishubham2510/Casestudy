package com.myreflectionthoughts.covidstat.datasource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import com.myreflectionthoughts.covidstat.contract.IDataSource;
import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.externaldto.LastTwoDaysResponse;
import com.myreflectionthoughts.covidstat.enums.USECASE;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import com.myreflectionthoughts.covidstat.exception.FallbackException;
import com.myreflectionthoughts.covidstat.registry.URLTemplateRegistry;
import com.myreflectionthoughts.covidstat.utility.DataUtility;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.common.util.StringUtils;
import io.micrometer.context.ContextSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class RemoteDataSource implements IDataSource<ResponseWrapper> {

    private final Logger logger;
    private final URLTemplateRegistry urlTemplateRegistry;
    private final IRemoteConnection<String> remoteConnection;
    private final MappingUtility mappingUtility;
    private static final Map<String, String> defaultHeaders;

    static {
        defaultHeaders = new HashMap<>();
        defaultHeaders.put("accept", "application/json");
    }

    public RemoteDataSource(IRemoteConnection<String> remoteConnection){
        this.urlTemplateRegistry = URLTemplateRegistry.getURLUrlTemplateRegistryInstance();
        this.mappingUtility = MappingUtility.getMappingUtilityInstance();
        this.remoteConnection = remoteConnection;
        this.logger = Logger.getLogger(RemoteDataSource.class.getSimpleName());
    }

    @Override
    @Retry(name = "latestCountryStats")
    @CircuitBreaker(name="latestCountryStats", fallbackMethod = "staticCountryStats")
    public ExternalAPIResponse getLatestStats(String country, long referencedDate) {
        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);
        url = prepareURLForLatestStat(url, country, "", "", "false", "true");
        String response = remoteConnection.executeGetRequest(url, defaultHeaders);
        ExternalAPIResponse externalAPIResponse = null;

        try {
            externalAPIResponse = this.mappingUtility.parseToPOJO(response, ExternalAPIResponse.class);
        } catch (JsonProcessingException e) {
            logger.severe("Error occurred while parsing response for latest stats, ex:- "+e.getMessage());
            throw new CaseStudyException(ServiceConstant._ERR_PARSING_ERROR_LATEST_STAT_KEY, 400, "Error occurred while parsing response for latest stats");
        }

        logger.info("Latest stat response for:- { "+country+" }, received/evaluated successfully");
        return externalAPIResponse;
    }

    @Override
    @Retry(name = "vaccineCoverageStats")
    @CircuitBreaker(name="vaccineCoverageStats", fallbackMethod = "staticVaccineCoverageStats")
    public ExternalAPIResponse getVaccineCoverage(String country, long referencedDate) {
        String url = this.urlTemplateRegistry.getURL(USECASE.VACCINE_COVERAGE);
        url = prepareURLForVaccineCoverage(url, country, String.valueOf(referencedDate),  "true");
        String response = remoteConnection.executeGetRequest(url, defaultHeaders);
        ExternalAPIResponse externalAPIResponse = null;


        try {

            if(StringUtils.isNotEmpty(country) && country.equalsIgnoreCase("global")){
                response = MappingUtility.adjustGlobalVaccineCoverageResponse(response);
                logger.info("Adjusted global response json");
            }

            externalAPIResponse = this.mappingUtility.parseToPOJO(response, ExternalAPIResponse.class);
        } catch (JsonProcessingException e) {
            logger.severe("Error occurred while parsing response for vaccine coverage, ex:- "+e.getMessage());
            throw new CaseStudyException(ServiceConstant._ERR_PARSING_ERROR_VACCINE_COVERAGE_KEY, 400, "Error occurred while parsing response for vaccine coverage stats");
        }

        logger.info("Vaccine coverage response for:- { "+country+" }, received/evaluated successfully");
        return externalAPIResponse;
    }

    @Override
    @Retry(name = "lastTwoDayStats")
    @CircuitBreaker(name="lastTwoDayStats", fallbackMethod = "staticLastTwoDayStats")
    public LastTwoDaysResponse getDataForAlerts(String country, long referencedDate) {

        LastTwoDaysResponse lastTwoDaysResponse = new LastTwoDaysResponse();


        // Can use Async request processing here

        CompletableFuture<ExternalAPIResponse> lastDayResponseFuture = fetchAsync(prepareURLForPreviousDay(country));
        CompletableFuture<ExternalAPIResponse> secondLastDayResponseFuture = fetchAsync(prepareURLForPreviousToPreviousDay(country));

        ContextSnapshot snapshot = ContextSnapshot.captureAll();

        return lastDayResponseFuture.thenCombine(secondLastDayResponseFuture, (response1, response2)-> {

            try (ContextSnapshot.Scope scope = snapshot.setThreadLocals()) {

                lastTwoDaysResponse.getLastTwoDaysResponse().add(response1);
                lastTwoDaysResponse.getLastTwoDaysResponse().add(response2);

                logger.info("Last two days data for alert, country:- { " + country + " }, received/evaluated successfully");

                return lastTwoDaysResponse;
            }

        }).join();
    }

    private String prepareURLForPreviousDay(String country){
        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);

        // last day
        return prepareURLForLatestStat(url, country, "true", "", "false", "true");
    }

    private String prepareURLForPreviousToPreviousDay(String country){
        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);

        // lastTwoDays
        return prepareURLForLatestStat(url, country, "", "true", "false", "true");
    }

    private String prepareURLForLatestStat(String baseURL, String country, String yesterday, String twoDaysAgo, String allowNull, String strict){

        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(baseURL);


        if(StringUtils.isNotBlank(country)){
            uri = uri.pathSegment(country);
        }

        if(StringUtils.isNotBlank(yesterday)){
            uri = uri.queryParam("yesterday", yesterday);
        }

        if(StringUtils.isNotBlank(twoDaysAgo)){
            uri = uri.queryParam("twoDaysAgo", twoDaysAgo);
        }

        if(StringUtils.isNotBlank(strict)){
            uri = uri.queryParam("strict", strict);
        }

        if(StringUtils.isNotBlank(allowNull)){
            uri = uri.queryParam("allowNull", allowNull);
        }

        return uri.build().encode().toUriString();
    }

    private String prepareURLForVaccineCoverage(String url, String country, String lastDays, String fullData){

        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromPath(url);

        if(StringUtils.isNotBlank(country) && !country.equalsIgnoreCase("global")){
            uriComponents = uriComponents.path("/countries").pathSegment(country);
        }

        // by default API returns latest of last 30 days data it has
        if(StringUtils.isNotBlank(lastDays)){
            uriComponents = uriComponents.queryParam("lastdays", lastDays);
        }

        if(StringUtils.isNotBlank(fullData)){
            uriComponents = uriComponents.queryParam("fullData", fullData);
        }else{
            uriComponents = uriComponents.queryParam("lastdays", "false");
        }

        return uriComponents.build().encode().toUriString();
    }

    /*
      Scenario:- If 1st call fails and any further call succeeds, it can cause wrong data sent to the customer
     */

    public ExternalAPIResponse staticCountryStats(String country, long referencedDate, Throwable t){
        ExternalAPIResponse externalAPIResponse =  DataUtility.getDefaultResponse("data/StaticLatestCountryStat.json", ExternalAPIResponse.class);

        if(StringUtils.isNotBlank(country) && country.equalsIgnoreCase(externalAPIResponse.getCountry())){
            externalAPIResponse.setServedFromCache(true);
            return externalAPIResponse;
        }

        throw new FallbackException();
    }

    public ExternalAPIResponse staticVaccineCoverageStats(String country, long referencedDate, Throwable t){
        ExternalAPIResponse externalAPIResponse =  DataUtility.getDefaultResponse("data/StaticVaccineCoverageStats.json", ExternalAPIResponse.class);


        if(StringUtils.isNotBlank(country) && country.equalsIgnoreCase(externalAPIResponse.getCountry())){
            externalAPIResponse.setServedFromCache(true);
            return externalAPIResponse;
        }

        throw new FallbackException();
    }

    public LastTwoDaysResponse staticLastTwoDayStats(String country, long referencedDate, Throwable t){
        LastTwoDaysResponse lastTwoDaysResponse =  DataUtility.getDefaultResponse("data/StaticTwoDayStats.json", LastTwoDaysResponse.class);

        if(StringUtils.isNotBlank(country) && country.equalsIgnoreCase(lastTwoDaysResponse.getLastTwoDaysResponse().get(0).getCountry())){
            lastTwoDaysResponse.getLastTwoDaysResponse().forEach(externalAPIResponse -> {
                externalAPIResponse.setServedFromCache(true);
            });
            return lastTwoDaysResponse;
        }

        throw new FallbackException();

    }

//    private CompletableFuture<ExternalAPIResponse> fetchAsync(String url){
//
//        return CompletableFuture.supplyAsync(()->{
//            String response = remoteConnection.executeGetRequest(url, defaultHeaders);
//            try {
//                return this.mappingUtility.parseToPOJO(response, ExternalAPIResponse.class);
//            } catch (JsonProcessingException e) {
//                logger.severe("Error occurred while parsing response for latest stats, ex:- "+e.getMessage());
//                throw new CaseStudyException(ServiceConstant._ERR_PARSING_ERROR_DAILY_STAT_KEY, 400, "Error occurred while parsing response for daily stats");
//            }
//        });
//    }

    private CompletableFuture<ExternalAPIResponse> fetchAsync(String url){

        ContextSnapshot snapshot = ContextSnapshot.captureAll();

        return CompletableFuture.supplyAsync(() -> {

            try (ContextSnapshot.Scope scope = snapshot.setThreadLocals()) {

                String response = remoteConnection.executeGetRequest(url, defaultHeaders);

                try {
                    return this.mappingUtility.parseToPOJO(response, ExternalAPIResponse.class);
                } catch (JsonProcessingException e) {
                    logger.severe("Error occurred while parsing response for latest stats, ex:- "+e.getMessage());
                    throw new CaseStudyException(
                            ServiceConstant._ERR_PARSING_ERROR_DAILY_STAT_KEY,
                            400,
                            "Error occurred while parsing response for daily stats"
                    );
                }

            }
        });
    }
}
