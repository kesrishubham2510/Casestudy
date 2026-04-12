package com.myreflectionthoughts.covidstat.service.impl.populator;

import com.myreflectionthoughts.covidstat.contract.ICacheFacade;
import com.myreflectionthoughts.covidstat.contract.IDataSource;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CountryStatResponsePopulatorTest {

    private final ICacheFacade<String, ExternalAPIResponse> redisCacheService;
    private final IDataSource<ResponseWrapper> remoteDataSource;
    private final CountryStatResponsePopulator populator;

    public CountryStatResponsePopulatorTest() {
        redisCacheService = Mockito.mock(ICacheFacade.class);
        remoteDataSource = Mockito.mock(IDataSource.class);

        populator = new CountryStatResponsePopulator(redisCacheService, remoteDataSource);
    }

    // =========================
    // ✅ CACHE HIT
    // =========================
    @Test
    void testPopulate_cacheHit() {

        ExternalAPIResponse cachedResponse = mock(ExternalAPIResponse.class);

        when(cachedResponse.getActive()).thenReturn(100L);
        when(cachedResponse.getCases()).thenReturn(1000L);
        when(cachedResponse.getRecovered()).thenReturn(900L);

        when(redisCacheService.get(anyString())).thenReturn(cachedResponse);

        CovidStatResponse result =
                populator.populate("India", "2024-02-02");

        assertNotNull(result);
        assertEquals("India", result.getCountry());
        assertEquals("100", result.getActiveAsToday());
        assertEquals("1000", result.getNoOfCases());
        assertEquals("900", result.getNoOfRecoveries());

        // ✅ Remote NOT called
        verify(remoteDataSource, never()).getLatestStats(anyString(), anyLong());
    }

    // =========================
    // ❌ CACHE MISS
    // =========================
    @Test
    void testPopulate_cacheMiss() {

        when(redisCacheService.get(anyString())).thenReturn(null);

        ExternalAPIResponse apiResponse = mock(ExternalAPIResponse.class);

        when(apiResponse.getActive()).thenReturn(200L);
        when(apiResponse.getCases()).thenReturn(2000L);
        when(apiResponse.getRecovered()).thenReturn(1500L);

        when(remoteDataSource.getLatestStats(eq("India"), anyLong()))
                .thenReturn(apiResponse);

        doNothing().when(redisCacheService).put(any());

        CovidStatResponse result =
                populator.populate("India", "2024-02-02");

        assertNotNull(result);
        assertEquals("India", result.getCountry());
        assertEquals("200", result.getActiveAsToday());
        assertEquals("2000", result.getNoOfCases());
        assertEquals("1500", result.getNoOfRecoveries());

        // ✅ Remote called
        verify(remoteDataSource, times(1))
                .getLatestStats(eq("India"), anyLong());

        // ✅ Cache populated
        verify(redisCacheService, times(1))
                .put(apiResponse);
    }

    // =========================
    // ⚠️ DEFAULT DATE CASE
    // =========================
    @Test
    void testPopulate_blankReferencedDate() {

        ExternalAPIResponse cachedResponse = mock(ExternalAPIResponse.class);

        when(cachedResponse.getActive()).thenReturn(50L);
        when(cachedResponse.getCases()).thenReturn(500L);
        when(cachedResponse.getRecovered()).thenReturn(400L);

        when(redisCacheService.get(anyString())).thenReturn(cachedResponse);

        CovidStatResponse result =
                populator.populate("India", "");

        assertNotNull(result);

        // ✅ default applied
        assertEquals("02-02-2021", result.getReferencedDate());
    }
}