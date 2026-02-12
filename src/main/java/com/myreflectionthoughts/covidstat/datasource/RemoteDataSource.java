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
import com.myreflectionthoughts.covidstat.registry.URLTemplateRegistry;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import io.micrometer.common.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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

    public RemoteDataSource(URLTemplateRegistry urlTemplateRegistry, IRemoteConnection<String> remoteConnection, MappingUtility mappingUtility){
        this.urlTemplateRegistry = urlTemplateRegistry;
        this.mappingUtility = mappingUtility;
        this.remoteConnection = remoteConnection;
        this.logger = Logger.getLogger(RemoteDataSource.class.getSimpleName());
    }

    @Override
    public ExternalAPIResponse getLatestStats(String country, long referencedDate) {
        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);
        url = prepareURL(url, country, "", "", "false", "true");
        String response = remoteConnection.executeGetRequest(url, defaultHeaders);
        ExternalAPIResponse externalAPIResponse = null;


        try {
            externalAPIResponse = this.mappingUtility.parseToPOJO(response, ExternalAPIResponse.class);
        } catch (JsonProcessingException e) {
            logger.severe("Error occurred while parsing response for latest stats, ex:- "+e.getMessage());
            throw new CaseStudyException(ServiceConstant._ERR_PARSING_ERROR_LATEST_STAT_KEY, 400, "Error occurred while parsing response for latest stats");
        }

        return externalAPIResponse;
    }

    @Override
    public ExternalAPIResponse getVaccineCoverage(String country, long referencedDate) {
        String url = this.urlTemplateRegistry.getURL(USECASE.VACCINE_COVERAGE);
        url = prepareURL(url, country, String.valueOf(referencedDate),  "true");
        String response = remoteConnection.executeGetRequest(url, defaultHeaders);
        ExternalAPIResponse externalAPIResponse = null;


        try {

            if(StringUtils.isEmpty(country)){
                response = MappingUtility.adjustGlobalVaccineCoverageResponse(response);
            }

            externalAPIResponse = this.mappingUtility.parseToPOJO(response, ExternalAPIResponse.class);
        } catch (JsonProcessingException e) {
            logger.severe("Error occurred while parsing response for vaccine coverage, ex:- "+e.getMessage());
            throw new CaseStudyException(ServiceConstant._ERR_PARSING_ERROR_VACCINE_COVERAGE_KEY, 400, "Error occurred while parsing response for vaccine coverage stats");
        }

        return externalAPIResponse;
    }

    @Override
    public LastTwoDaysResponse getDataForAlerts(String country, long referencedDate) {

        String url = null;
        ExternalAPIResponse externalAPIResponse = null;
        LastTwoDaysResponse lastTwoDaysResponse = new LastTwoDaysResponse();
        String response = null;


        try {

            url = prepareURLForPreviousDay(country);
            response = remoteConnection.executeGetRequest(url, defaultHeaders);
            externalAPIResponse = this.mappingUtility.parseToPOJO(response, ExternalAPIResponse.class);
            lastTwoDaysResponse.getLastTwoDaysResponse().add(externalAPIResponse);

            url = prepareURLForPreviousToPreviousDay(country);
            response = remoteConnection.executeGetRequest(url, defaultHeaders);
            externalAPIResponse = this.mappingUtility.parseToPOJO(response, ExternalAPIResponse.class);
            lastTwoDaysResponse.getLastTwoDaysResponse().add(externalAPIResponse);

        } catch (JsonProcessingException e) {
            logger.severe("Error occurred while parsing response for latest stats, ex:- "+e.getMessage());
            throw new CaseStudyException(ServiceConstant._ERR_PARSING_ERROR_DAILY_STAT_KEY, 400, "Error occurred while parsing response for daily stats");
        }

        return lastTwoDaysResponse;
    }

    private String prepareURLForPreviousDay(String country){
        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);

        // last day
        return prepareURL(url, country, "true", "", "false", "true");
    }

    private String prepareURLForPreviousToPreviousDay(String country){
        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);

        // lastTwoDays
        return prepareURL(url, country, "", "true", "false", "true");
    }



    private String prepareURL(String url, String country, String yesterday, String twoDaysAgo, String allowNull, String strict){

        url = url.replace("{country}", country);

        if(StringUtils.isBlank(yesterday)){
            url = url.replace("yesterday={yesterday}&", "");
        }else{
            url = url.replace("{yesterday}", yesterday);
        }

        if(StringUtils.isBlank(twoDaysAgo)){
            url = url.replace("twoDaysAgo={twoDaysAgo}&", "");
        }else{
            url = url.replace("{twoDaysAgo}", twoDaysAgo);
        }

        url = url.replace("{strict}", strict);
        url = url.replace("{allowNull}", allowNull);

        return url;
    }

    private String prepareURL(String url, String country, String lastDays, String fullData){

        if(StringUtils.isBlank(country)){
            url = url.replace("countries/{country}", "");
        }else {
            url = url.replace("{country}", country);
        }

        // by default API returns latest of last 30 days data it has
        if(StringUtils.isBlank(lastDays) || lastDays.equalsIgnoreCase("0")){
            url = url.replace("lastdays={lastdays}&", "");
        }else {
            url = url.replace("{lastdays}", lastDays);
        }

        if(StringUtils.isBlank(fullData)){
          fullData = "false";
        }

        url = url.replace("{fullData}", fullData);
        return url;
    }
}
