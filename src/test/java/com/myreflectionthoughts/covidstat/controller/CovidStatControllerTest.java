package com.myreflectionthoughts.covidstat.controller;

import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.service.Orchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

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
        when(orchestrator.fetchStats("india", "2024-01-01"))
                .thenReturn(mockResponse);

        ResponseEntity<CovidStatResponse> response =
                controller.getCountryStats("india", "2024-01-01");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());

        verify(orchestrator).fetchStats("india", "2024-01-01");
    }

    @Test
    void getCountryStats_DefaultReferencedDate() {

        CovidStatResponse mockResponse = new CovidStatResponse();
        when(orchestrator.fetchStats("india", ""))
                .thenReturn(mockResponse);

        ResponseEntity<CovidStatResponse> response =
                controller.getCountryStats("india", "");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());

        verify(orchestrator).fetchStats("india", "");
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

        when(orchestrator.fetchComparisionStats(any(), eq("2024-01-01")))
                .thenReturn(mockList);

        ResponseEntity<List<CovidStatResponse>> response =
                controller.getCountryComparisonStats(
                        "2024-01-01",
                        "india",
                        "usa",
                        "",
                        ""
                );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockList, response.getBody());

        verify(orchestrator).fetchComparisionStats(any(), eq("2024-01-01"));
    }

    @Test
    void getCountryComparisonStats_DefaultParams() {

        when(orchestrator.fetchComparisionStats(any(), eq("")))
                .thenReturn(List.of());

        ResponseEntity<List<CovidStatResponse>> response =
                controller.getCountryComparisonStats(
                        "",
                        "",
                        "",
                        "",
                        ""
                );

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());

        verify(orchestrator).fetchComparisionStats(any(), eq(""));
    }
}