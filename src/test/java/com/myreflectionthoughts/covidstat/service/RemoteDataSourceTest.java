package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.contract.IDataSource;
import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.datasource.RemoteDataSource;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.externaldto.LastTwoDaysResponse;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import com.myreflectionthoughts.covidstat.registry.URLTemplateRegistry;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class RemoteDataSourceTest {

    private final URLTemplateRegistry urlTemplateRegistry;
    private final IRemoteConnection<String> remoteConnection;
    private final MappingUtility mappingUtility;

    private final IDataSource<ResponseWrapper> mockRemoteDataSource;
    private final IRemoteConnection<String> mockRemoteConnection;

    private final RemoteDataSource remoteDataSource;
    private final String country;

    public RemoteDataSourceTest(){
        this.urlTemplateRegistry = new URLTemplateRegistry();
        this.remoteConnection = new HttpConnection(HttpClient.newHttpClient());
        this.mappingUtility = new MappingUtility();
        this.mockRemoteConnection = Mockito.mock(HttpConnection.class);
        this.country = "India";
        this.remoteDataSource = new RemoteDataSource(urlTemplateRegistry, remoteConnection, mappingUtility);
        this.mockRemoteDataSource = new RemoteDataSource(urlTemplateRegistry, mockRemoteConnection, mappingUtility);
    }

    @Test
    public void testGetLatestStats(){
       ExternalAPIResponse externalAPIResponse = remoteDataSource.getLatestStats(country, 0L);
       assertEquals(externalAPIResponse.getCountry(), "India");
    }

    @Test
    public void testGetLatestStats_Throws_Fails_LatestStat_Parsing(){
        when(mockRemoteConnection.executeGetRequest(anyString(), anyMap())).thenReturn("{]");
        CaseStudyException exception = assertThrows(CaseStudyException.class, ()-> mockRemoteDataSource.getLatestStats(country, 0L));
        assertEquals("PARSING_ERROR_LATEST_STAT", exception.getKey());
    }

    @Test
    public void testGetVaccineCoverageForAMonth(){
        ExternalAPIResponse externalAPIResponse = remoteDataSource.getVaccineCoverage(country, 0L);
        assertEquals(externalAPIResponse.getTimeline().size(), 30);
    }

    @Test
    public void testGetLatestStats_Throws_Fails_VaccineCoverage_Parsing(){
        when(mockRemoteConnection.executeGetRequest(anyString(), anyMap())).thenReturn("{]");
        CaseStudyException exception = assertThrows(CaseStudyException.class, ()-> mockRemoteDataSource.getVaccineCoverage(country, 0L));
        assertEquals("PARSING_ERROR_VACCINE_COVERAGE", exception.getKey());
    }

    @Test
    public void testGetVaccineCoverageForLast3Days(){
        ExternalAPIResponse externalAPIResponse = remoteDataSource.getVaccineCoverage(country, 3L);
        assertEquals(externalAPIResponse.getTimeline().size(), 3);
    }

    @Test
    public void testGetDataForAlerts(){
        LastTwoDaysResponse lastTwoDaysResponse = remoteDataSource.getDataForAlerts(country, 0L);
        assertNotEquals(lastTwoDaysResponse.getLastTwoDaysResponse().get(0).getUpdated(), lastTwoDaysResponse.getLastTwoDaysResponse().get(1).getUpdated());
    }

    @Test
    public void testGetLatestStats_Throws_Fails_DailyAlert_Parsing(){
        when(mockRemoteConnection.executeGetRequest(anyString(), anyMap())).thenReturn("{]");
        CaseStudyException exception = assertThrows(CaseStudyException.class, ()-> mockRemoteDataSource.getDataForAlerts(country, 0L));
        assertEquals("PARSING_ERROR_DAILY_STAT", exception.getKey());
    }


}
