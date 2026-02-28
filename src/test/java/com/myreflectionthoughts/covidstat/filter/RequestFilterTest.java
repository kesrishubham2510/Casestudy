package com.myreflectionthoughts.covidstat.filter;

import com.myreflectionthoughts.covidstat.config.APIKeyConfig;
import com.myreflectionthoughts.covidstat.config.CountryConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestFilterTest {

    private RequestFilter requestFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        CountryConfig countryConfig = mock(CountryConfig.class);
        APIKeyConfig apiKeyConfig = mock(APIKeyConfig.class);

        when(apiKeyConfig.getKey()).thenReturn("valid-key");

        requestFilter = new RequestFilter(countryConfig, apiKeyConfig);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    // =============================
    // shouldNotFilter Tests
    // =============================

    @Test
    void shouldNotFilter_ForOptionsMethod() {
        when(request.getMethod()).thenReturn("OPTIONS");
        when(request.getRequestURI()).thenReturn("/any");

        assertTrue(requestFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ForSwaggerURI() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        assertTrue(requestFilter.shouldNotFilter(request));
    }

    @Test
    void shouldFilter_ForNormalRequest() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/covid");

        assertFalse(requestFilter.shouldNotFilter(request));
    }

    // =============================
    // API Key Tests
    // =============================

    @Test
    void doFilterInternal_ShouldReturn401_WhenHeaderMissing() throws Exception {

        when(request.getHeader("API-KEY")).thenReturn(null);

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        requestFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(sw.toString().contains("Invalid API Key"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_ShouldReturn401_WhenHeaderInvalid() throws Exception {

        when(request.getHeader("API-KEY")).thenReturn("wrong-key");

        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        requestFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(sw.toString().contains("Invalid API Key"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_ShouldProceed_WhenHeaderValid() throws Exception {

        when(request.getHeader("API-KEY")).thenReturn("valid-key");

        requestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}