package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WebClientConnectionTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;

    @Mock
    private WebClient.RequestHeadersSpec headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private WebClientConnection connection;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        connection = new WebClientConnection(
                webClient
        );
    }

    @Test
    void testExecuteGetRequest_success() {

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri((URI) any())).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just("SUCCESS"));

        String result = connection.executeGetRequest("/test", Map.of());

        assertEquals("SUCCESS", result);
    }

    @Test
    void testExecuteGetRequest_4xxError() {

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri((URI) any())).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        // simulate error triggered inside pipeline
        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new CaseStudyException("BAD_REQUEST", 400, "error")));

        assertThrows(CaseStudyException.class,
                     () -> connection.executeGetRequest("/test", Map.of()));
    }


    @Test
    void testExecuteGetRequest_5xxError() {

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri((URI) any())).thenReturn(headersSpec);
        when(headersSpec.headers(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any()))
                .thenReturn(responseSpec);

        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new CaseStudyException("SERVER_ERROR", 500, "error")));

        assertThrows(CaseStudyException.class,
                     () -> connection.executeGetRequest("/test", Map.of()));
    }

    @Test
    void testExecuteGetRequest_genericException() {

        when(webClient.get()).thenThrow(new RuntimeException("Connection failed"));

        CaseStudyException ex = assertThrows(CaseStudyException.class,
                                             () -> connection.executeGetRequest("/test", Map.of()));

        assertNotNull(ex);
    }
}