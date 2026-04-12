package com.myreflectionthoughts.covidstat.service.impl.populator;

import com.myreflectionthoughts.covidstat.contract.ICacheFacade;
import com.myreflectionthoughts.covidstat.contract.IDataSource;
import com.myreflectionthoughts.covidstat.contract.ITrendEvaluation;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.Trends;
import com.myreflectionthoughts.covidstat.entity.externaldto.CoverageStatTimeline;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CovidStatTrendPopulatorTest {

    private final ICacheFacade<String, HashMap<String, Trends>> redisCacheTrendService;
    private final IDataSource<ResponseWrapper> remoteDataSource;
    private final CovidStatTrendPopulator populator;
    private final ITrendEvaluation<ExternalAPIResponse, ResponseWrapper> trendEvaluation;


    public CovidStatTrendPopulatorTest() {
        redisCacheTrendService = Mockito.mock(ICacheFacade.class);
        remoteDataSource = Mockito.mock(IDataSource.class);
        trendEvaluation = Mockito.mock(ITrendEvaluation.class);
        populator = new CovidStatTrendPopulator(redisCacheTrendService, remoteDataSource, trendEvaluation);
    }


    @Test
    void testPopulate_cacheHit() {

        HashMap<String, Trends> globalMap = new HashMap<>();
        globalMap.put("global", mock(Trends.class));

        HashMap<String, Trends> countryMap = new HashMap<>();
        countryMap.put("India", mock(Trends.class));

        when(redisCacheTrendService.get(anyString()))
                .thenReturn(globalMap)   // first call
                .thenReturn(countryMap); // second call

        CovidStatResponse response =
                populator.populate("India", "2024-02-02");

        assertNotNull(response);
        assertNotNull(response.getTrends());
        assertEquals(2, response.getTrends().size());

        verify(remoteDataSource, never()).getVaccineCoverage(anyString(), anyLong());
    }


    @Test
    void testPopulate_cacheMiss() {

        when(redisCacheTrendService.get(anyString())).thenReturn(null);

        // Mock ExternalAPIResponse (country)
        ExternalAPIResponse countryResponse = mock(ExternalAPIResponse.class);
        ExternalAPIResponse globalResponse = mock(ExternalAPIResponse.class);

        // Timeline mock
        CoverageStatTimeline timelineEntry = mock(CoverageStatTimeline.class);
        when(timelineEntry.getTotal()).thenReturn("1000");

        when(countryResponse.getTimeline()).thenReturn(List.of(timelineEntry));
        when(globalResponse.getTimeline()).thenReturn(List.of(timelineEntry));

        when(remoteDataSource.getVaccineCoverage(eq("India"), anyLong()))
                .thenReturn(countryResponse);

        when(remoteDataSource.getVaccineCoverage(eq("global"), anyLong()))
                .thenReturn(globalResponse);

        when(trendEvaluation.calculate(any(ExternalAPIResponse.class), any())).thenReturn(new Trends());
        doNothing().when(redisCacheTrendService).put(any());

        CovidStatResponse response =
                populator.populate("India", "2024-02-02");

        assertNotNull(response);
        assertNotNull(response.getTrends());

        verify(remoteDataSource, atLeastOnce())
                .getVaccineCoverage(anyString(), anyLong());

        verify(redisCacheTrendService, times(1)).put(any());
    }
}