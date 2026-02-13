package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import com.myreflectionthoughts.covidstat.contract.*;
import com.myreflectionthoughts.covidstat.datasource.RemoteDataSource;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.externaldto.LastTwoDaysResponse;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import com.myreflectionthoughts.covidstat.handler.BadRequestExceptionHandler;
import com.myreflectionthoughts.covidstat.handler.ConnectionExceptionHandler;
import com.myreflectionthoughts.covidstat.handler.DataProcessingExceptionHandler;
import com.myreflectionthoughts.covidstat.handler.GenericExceptionHandler;
import com.myreflectionthoughts.covidstat.utility.CacheUtility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class OrchestratorTest {

    private final IDataSource<ResponseWrapper> remoteDataSource;
    private final ICache<String, String> cacheService;
    private final IExceptionHandler<CaseStudyException, Void> badRequesIExceptionHandler;
    private final IExceptionHandler<CaseStudyException, Void> connectionExceptionHandler;
    private final IExceptionHandler<CaseStudyException, Void> genericExceptionHandler;
    private final IExceptionHandler<CaseStudyException, Void> dataProcessingExceptionHandler;

    private final IRemoteConnection<String> httpConnection;

    private final IDataSource<ResponseWrapper> mockedRemoteDataSource;
    private final ICache<String, String> mockedCacheService;
    // Will have to do injection using beanName here
    private final IRemoteConnection<String> mockedHttpConnection;
    private final Orchestrator orchestrator;
    private final Orchestrator mockedOrchestrator;


    public OrchestratorTest() {
        this.httpConnection = Mockito.mock(HttpConnection.class);
        this.remoteDataSource = Mockito.mock(RemoteDataSource.class);
        this.cacheService = Mockito.mock(RedisCacheService.class);
        this.badRequesIExceptionHandler = Mockito.mock(BadRequestExceptionHandler.class);
        this.connectionExceptionHandler = Mockito.mock(ConnectionExceptionHandler.class);
        this.genericExceptionHandler = Mockito.mock(GenericExceptionHandler.class);
        this.dataProcessingExceptionHandler = Mockito.mock(DataProcessingExceptionHandler.class);


        this.mockedHttpConnection = Mockito.mock(HttpConnection.class);
        this.mockedRemoteDataSource = new RemoteDataSource(mockedHttpConnection);
        this.mockedCacheService = Mockito.mock(RedisCacheService.class);
        this.orchestrator = new Orchestrator(remoteDataSource, cacheService, badRequesIExceptionHandler, connectionExceptionHandler, genericExceptionHandler, dataProcessingExceptionHandler);
        this.mockedOrchestrator = new Orchestrator(mockedRemoteDataSource, mockedCacheService, badRequesIExceptionHandler, connectionExceptionHandler, genericExceptionHandler, dataProcessingExceptionHandler);
    }

//    @Test
    public void testFetchStats(){
        CovidStatResponse statResponse =  this.orchestrator.fetchStats("India", null);
        System.out.println(statResponse);
    }

    @Test
    public void testFetchStats_WithoutCache(){

        String referenceDate = null;
        String latestCovidResponse = TestDataUtility.getFileContent("data/LatestCovidStat.json");
        String countryVaccineCoverage = TestDataUtility.getFileContent("data/VaccineCoverage_India_Last_25_Days.json");
        ExternalAPIResponse globalVaccineCoverageResponse = TestDataUtility.getGlobalVaccineCoverageData("VaccineCoverage_Global_Last_25_Days");
        String yesterdayStat = TestDataUtility.getFileContent("data/yesterdayStat.json");
        String dayBeforeYesterdayStat = TestDataUtility.getFileContent("data/dayBeforeYesterdayStat.json");
        LastTwoDaysResponse lastTwoDaysResponse = new LastTwoDaysResponse();
        lastTwoDaysResponse.getLastTwoDaysResponse().add(TestDataUtility.convertTOPOJO(yesterdayStat, ExternalAPIResponse.class));
        lastTwoDaysResponse.getLastTwoDaysResponse().add(TestDataUtility.convertTOPOJO(dayBeforeYesterdayStat, ExternalAPIResponse.class));

        when(cacheService.get(anyString())).thenReturn(null);
        when(remoteDataSource.getLatestStats(anyString(), anyLong())).thenReturn(TestDataUtility.convertTOPOJO(latestCovidResponse, ExternalAPIResponse.class));
        doNothing().when(cacheService).put(anyString(), anyString(), anyLong());
        when(remoteDataSource.getVaccineCoverage(anyString(), anyLong())).thenReturn(TestDataUtility.convertTOPOJO(countryVaccineCoverage, ExternalAPIResponse.class));
        when(remoteDataSource.getVaccineCoverage(eq("global"), eq(14))).thenReturn(globalVaccineCoverageResponse);
        doNothing().when(cacheService).put(anyString(), anyString(), anyLong());
        when(remoteDataSource.getDataForAlerts(anyString(), anyLong())).thenReturn(lastTwoDaysResponse);

        CovidStatResponse covidStatResponse = orchestrator.fetchStats("India", null);
        assertNotNull(covidStatResponse);
    }

    @Test
    public void testFetchStats_CacheHasLatestStat(){

        String country = "India";
        String referenceDate = "";
        String latestCovidResponse = TestDataUtility.getFileContent("data/LatestCovidStat.json");
        String mockedComputedResponse = TestDataUtility.getFileContent("data/MockedCovidStatResponse.json");

        when(cacheService.get(anyString())).thenReturn("{}");
        when(cacheService.get(eq(country))).thenReturn(mockedComputedResponse);

        CovidStatResponse covidStatResponse = orchestrator.fetchStats("India", referenceDate);
        assertNotNull(covidStatResponse);
    }

    @Test
    public void testFetchStats_CacheHasLatestStat_HandlesCaseStudyException(){

        String referenceDate = null;
        String latestCovidResponse = TestDataUtility.getFileContent("data/LatestCovidStat.json");
        String countryVaccineCoverage = TestDataUtility.getFileContent("data/VaccineCoverage_India_Last_25_Days.json");
        ExternalAPIResponse globalVaccineCoverageResponse = TestDataUtility.getGlobalVaccineCoverageData("VaccineCoverage_Global_Last_25_Days");
        String yesterdayStat = TestDataUtility.getFileContent("data/yesterdayStat.json");
        String dayBeforeYesterdayStat = TestDataUtility.getFileContent("data/dayBeforeYesterdayStat.json");
        LastTwoDaysResponse lastTwoDaysResponse = new LastTwoDaysResponse();
        lastTwoDaysResponse.getLastTwoDaysResponse().add(TestDataUtility.convertTOPOJO(yesterdayStat, ExternalAPIResponse.class));
        lastTwoDaysResponse.getLastTwoDaysResponse().add(TestDataUtility.convertTOPOJO(dayBeforeYesterdayStat, ExternalAPIResponse.class));

        when(cacheService.get(anyString())).thenReturn(null);
        when(remoteDataSource.getLatestStats(anyString(), anyLong())).thenThrow(new CaseStudyException(ServiceConstant._ERR_BAD_REQUEST_KEY, 400, "BadRequest"));
        doNothing().when(cacheService).put(anyString(), anyString(), anyLong());
        when(remoteDataSource.getVaccineCoverage(anyString(), anyLong())).thenReturn(TestDataUtility.convertTOPOJO(countryVaccineCoverage, ExternalAPIResponse.class));
        when(remoteDataSource.getVaccineCoverage(eq("global"), eq(14))).thenReturn(globalVaccineCoverageResponse);
        doNothing().when(cacheService).put(anyString(), anyString(), anyLong());
        when(remoteDataSource.getDataForAlerts(anyString(), anyLong())).thenReturn(lastTwoDaysResponse);

        CovidStatResponse covidStatResponse = orchestrator.fetchStats("India", null);
        assertNotNull(covidStatResponse);
    }
}
