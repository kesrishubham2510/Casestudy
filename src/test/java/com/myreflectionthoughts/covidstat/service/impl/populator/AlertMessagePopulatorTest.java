package com.myreflectionthoughts.covidstat.service.impl.populator;

import com.myreflectionthoughts.covidstat.constant.Country;
import com.myreflectionthoughts.covidstat.contract.ICacheFacade;
import com.myreflectionthoughts.covidstat.contract.IDataSource;
import com.myreflectionthoughts.covidstat.datasource.RemoteDataSource;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.externaldto.LastTwoDaysResponse;
import com.myreflectionthoughts.covidstat.service.TestDataUtility;
import com.myreflectionthoughts.covidstat.service.impl.cacheservice.CovidStatRawResponseCacheService;
import com.myreflectionthoughts.covidstat.service.impl.cacheservice.RedisAlertMessageCacheService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class AlertMessagePopulatorTest {

    private final ICacheFacade<String, String> redisAlertMessagePopulatorService;
    private final ICacheFacade<String, ExternalAPIResponse> redisCacheExternalAPIResponseService;
    private final IDataSource<ResponseWrapper> remoteDataSource;
    private final AlertMessagePopulator alertMessagePopulator;

    public AlertMessagePopulatorTest(){
        this.redisAlertMessagePopulatorService = Mockito.mock(RedisAlertMessageCacheService.class);
        this.redisCacheExternalAPIResponseService = Mockito.mock(CovidStatRawResponseCacheService.class);
        this.remoteDataSource = Mockito.mock(IDataSource.class);
        this.alertMessagePopulator = new AlertMessagePopulator(redisAlertMessagePopulatorService, redisCacheExternalAPIResponseService,remoteDataSource);
    }

    @Test
    void testPopulate_CacheSuccess(){
        when(redisAlertMessagePopulatorService.get(anyString())).thenReturn("alertMessage");
        assertNotNull(alertMessagePopulator.populate(Country.IRELAND.getDisplayName(), "2024-02-02"));
    }

    @Test
    void testPopulate_CacheMiss(){

        LastTwoDaysResponse response = new LastTwoDaysResponse();
        response.getLastTwoDaysResponse().add(TestDataUtility.convertTOPOJO(TestDataUtility.getFileContent("data/LatestCovidStat.json"), ExternalAPIResponse.class));
        response.getLastTwoDaysResponse().add(TestDataUtility.convertTOPOJO(TestDataUtility.getFileContent("data/dayBeforeYesterdayStat.json"), ExternalAPIResponse.class));

        when(redisAlertMessagePopulatorService.get(anyString())).thenReturn("");
        when(remoteDataSource.getDataForAlerts(anyString(), anyLong())).thenReturn(response);
        when(redisCacheExternalAPIResponseService.get(anyString())).thenReturn(TestDataUtility.convertTOPOJO(TestDataUtility.getFileContent("data/LatestCovidStat.json"), ExternalAPIResponse.class));
        doNothing().when(redisAlertMessagePopulatorService).put(anyString());
        assertNotNull(alertMessagePopulator.populate(Country.IRELAND.getDisplayName(), "2024-02-02"));
    }
}
