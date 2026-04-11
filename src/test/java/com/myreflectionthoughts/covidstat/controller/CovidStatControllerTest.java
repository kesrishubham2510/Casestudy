package com.myreflectionthoughts.covidstat.controller;

import com.myreflectionthoughts.covidstat.constant.Country;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.service.Orchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CovidStatControllerTest {

    private Orchestrator orchestrator;
    private CovidStatController controller;

    @BeforeEach
    void setUp() {
        orchestrator = mock(Orchestrator.class);
        controller = new CovidStatController(orchestrator);
    }

    // =========================================
    // getCountryStats Tests
    // =========================================

    @Test
    void getCountryStats_ShouldReturn200AndBody() {

        CovidStatResponse mockResponse = new CovidStatResponse();
        when(orchestrator.fetchStats(Country.INDIA, LocalDate.now()))
                .thenReturn(mockResponse);

        ResponseEntity<CovidStatResponse> response =
                controller.getCountryStats(Country.INDIA, LocalDate.now());

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());

        verify(orchestrator).fetchStats(Country.INDIA, LocalDate.now());
    }

    @Test
    void getCountryStats_DefaultReferencedDate() {

        CovidStatResponse mockResponse = new CovidStatResponse();
        when(orchestrator.fetchStats(Country.INDIA, null))
                .thenReturn(mockResponse);

        ResponseEntity<CovidStatResponse> response =
                controller.getCountryStats(Country.INDIA, null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());

        verify(orchestrator).fetchStats(Country.INDIA, null);
    }

    // =========================================
    // getCountryComparisonStats Tests
    // =========================================

    @Test
    void getCountryComparisonStats_ShouldReturn200AndList() {

        List<CovidStatResponse> mockList = List.of(
                new CovidStatResponse(),
                new CovidStatResponse()
        );

        when(orchestrator.fetchComparisionStats(any(), eq(LocalDate.now())))
                .thenReturn(mockList);

        ResponseEntity<List<CovidStatResponse>> response =
                controller.getCountryComparisonStats(
                        LocalDate.now(),
                        Country.INDIA,
                        Country.USA,
                        null,
                        null
                );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockList, response.getBody());

        verify(orchestrator).fetchComparisionStats(any(), eq(LocalDate.now()));
    }

    @Test
    void getCountryComparisonStats_DefaultParams() {

        when(orchestrator.fetchComparisionStats(any(), eq(LocalDate.now())))
                .thenReturn(List.of());

        ResponseEntity<List<CovidStatResponse>> response =
                controller.getCountryComparisonStats(
                        null,
                        null,
                        null,
                        null,
                        null
                );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());

        verify(orchestrator).fetchComparisionStats(any(), eq(LocalDate.now()));
    }
}