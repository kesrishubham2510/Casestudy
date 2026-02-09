package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.externaldto.LastTwoDaysResponse;
import com.myreflectionthoughts.covidstat.registry.URLTemplateRegistry;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class RemoteDataSourceTest {

    private final URLTemplateRegistry urlTemplateRegistry;
    private final IRemoteConnection<String> remoteConnection;
    private final MappingUtility mappingUtility;

    private final RemoteDataSource remoteDataSource;
    private final String country;

    public RemoteDataSourceTest(){
        this.urlTemplateRegistry = new URLTemplateRegistry();
        this.remoteConnection = new HttpConnection();
        this.mappingUtility = new MappingUtility();
        this.country = "India";
        this.remoteDataSource = new RemoteDataSource(urlTemplateRegistry, remoteConnection, mappingUtility);
    }

    @Test
    public void testGetLatestStats(){
       ExternalAPIResponse externalAPIResponse = remoteDataSource.getLatestStats(country, 0L);
       assertEquals(externalAPIResponse.getCountry(), "India");
    }

    @Test
    public void testGetVaccineCoverageForAMonth(){
        ExternalAPIResponse externalAPIResponse = remoteDataSource.getVaccineCoverage(country, 0L);
        assertEquals(externalAPIResponse.getTimeline().size(), 30);
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

}
