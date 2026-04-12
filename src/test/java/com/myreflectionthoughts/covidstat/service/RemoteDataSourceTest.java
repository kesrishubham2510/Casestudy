package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.constant.Country;
import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.datasource.RemoteDataSource;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import com.myreflectionthoughts.covidstat.utility.DataUtility;
import com.myreflectionthoughts.covidstat.utility.TestDataUtility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.externaldto.LastTwoDaysResponse;
import com.myreflectionthoughts.covidstat.exception.FallbackException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.externaldto.LastTwoDaysResponse;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import com.myreflectionthoughts.covidstat.exception.FallbackException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoteDataSourceTest {

    private final IRemoteConnection<String> remoteConnection;

    @InjectMocks
    private RemoteDataSource remoteDataSource;

    private ExternalAPIResponse mockResponse;

    public RemoteDataSourceTest(){
        remoteConnection = Mockito.mock(IRemoteConnection.class);
        this.remoteDataSource = new RemoteDataSource(remoteConnection);
    }

    @Test
    public void testGetLatestStats_Success(){

        when(remoteConnection.executeGetRequest(anyString(), anyMap()))
                .thenReturn(TestDataUtility.getFileContent("data/LatestCovidStat.json"));

        assertNotNull(remoteDataSource.getLatestStats(Country.INDIA.getDisplayName(), 1));
    }

    @Test
    public void testGetLatestStats_ThrowsaseStudyException(){

        when(remoteConnection.executeGetRequest(anyString(), anyMap()))
                .thenReturn("}");

        assertThrows(CaseStudyException.class, ()-> remoteDataSource.getLatestStats(Country.INDIA.getDisplayName(), 1));
    }

    @Test
    public void testGetVaccineCoverage_Success() {

        when(remoteConnection.executeGetRequest(anyString(), anyMap()))
                .thenReturn(TestDataUtility.getFileContent("data/VaccineCoverage_India_Last_25_Days.json"));

        ExternalAPIResponse response =
                remoteDataSource.getVaccineCoverage(Country.INDIA.getDisplayName(), 10);

        assertNotNull(response);
    }

    @Test
    public void testGetVaccineCoverage_ThrowsCaseStudyException() {

        when(remoteConnection.executeGetRequest(anyString(), anyMap()))
                .thenReturn("}");

        assertThrows(CaseStudyException.class,
                     () -> remoteDataSource.getVaccineCoverage(Country.INDIA.getDisplayName(), 10));
    }

    @Test
    public void testGetVaccineCoverage_Global_Success() {

        when(remoteConnection.executeGetRequest(anyString(), anyMap()))
                .thenReturn(TestDataUtility.getFileContent("data/VaccineCoverage_Global_Last_25_Days.json"));

        ExternalAPIResponse response =
                remoteDataSource.getVaccineCoverage("global", 10);

        assertNotNull(response);
    }

    @Test
    public void testGetDataForAlerts_Success() {

        String json = TestDataUtility.getFileContent("data/LatestCovidStat.json");

        when(remoteConnection.executeGetRequest(anyString(), anyMap()))
                .thenReturn(json);

        LastTwoDaysResponse response =
                remoteDataSource.getDataForAlerts(Country.INDIA.getDisplayName(), 1);

        assertNotNull(response);
        assertEquals(2, response.getLastTwoDaysResponse().size());
    }

    @Test
    public void testGetDataForAlerts_FirstCallParsingFails() {

        when(remoteConnection.executeGetRequest(anyString(), anyMap()))
                .thenReturn("}");
        assertThrows(CaseStudyException.class,
                     () -> remoteDataSource.getDataForAlerts(Country.INDIA.getDisplayName(), 1));
    }

    @Test
    public void testGetDataForAlerts_SecondCallParsingFails() {

        String validJson = TestDataUtility.getFileContent("data/LatestCovidStat.json");

        when(remoteConnection.executeGetRequest(anyString(), anyMap()))
                .thenReturn(validJson)
                .thenReturn("}");

        assertThrows(CaseStudyException.class,
                     () -> remoteDataSource.getDataForAlerts(Country.INDIA.getDisplayName(), 1));
    }

    @Test
    public void testStaticCountryStats_Success() {

        ExternalAPIResponse response =
                remoteDataSource.staticCountryStats(Country.AFGHANISTAN.getDisplayName(), 0);

        assertNotNull(response);
        assertTrue(response.isServedFromCache());
        assertEquals(Country.AFGHANISTAN.getDisplayName(), response.getCountry());
    }

    @Test
    public void testStaticCountryStats_ThrowsFallbackException() {

        assertThrows(FallbackException.class,
                     () -> remoteDataSource.staticCountryStats(Country.INDIA.getDisplayName(), 0));
    }

    @Test
    public void testStaticVaccineCoverageStats_Success() {

        ExternalAPIResponse response =
                remoteDataSource.staticVaccineCoverageStats(Country.AFGHANISTAN.getDisplayName(), 0);

        assertNotNull(response);
        assertTrue(response.isServedFromCache());
        assertEquals(Country.AFGHANISTAN.getDisplayName(), response.getCountry());
    }

    @Test
    public void testStaticVaccineCoverageStats_ThrowsFallbackException() {

        assertThrows(FallbackException.class,
                     () -> remoteDataSource.staticVaccineCoverageStats(Country.IRELAND.getDisplayName(), 0));
    }

    @Test
    public void testStaticLastTwoDayStats_Success() {

        LastTwoDaysResponse response =
                remoteDataSource.staticLastTwoDayStats(Country.AFGHANISTAN.getDisplayName(), 0);

        assertNotNull(response);
        assertEquals(2, response.getLastTwoDaysResponse().size());

        response.getLastTwoDaysResponse()
                .forEach(r -> assertTrue(r.isServedFromCache()));
    }

    @Test
    public void testStaticLastTwoDayStats_ThrowsFallbackException() {

        assertThrows(FallbackException.class,
                     () -> remoteDataSource.staticLastTwoDayStats(Country.DRC.getDisplayName(), 0));
    }

}