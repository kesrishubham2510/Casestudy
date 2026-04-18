package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
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

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @Mock
    private Tracer.SpanInScope spanInScope;

    private WebClientConnection connection;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(tracer.nextSpan()).thenReturn(span);
        when(span.name(anyString())).thenReturn(span);
        when(span.tag(anyString(), anyString())).thenReturn(span);
        when(span.start()).thenReturn(span);
        when(tracer.withSpan(span)).thenReturn(spanInScope);

        connection = new WebClientConnection(
                webClient,
                tracer
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
        verify(span).end();
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
        verify(span).error(any(CaseStudyException.class));
        verify(span).end();
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
        verify(span).error(any(CaseStudyException.class));
        verify(span).end();
    }

    @Test
    void testExecuteGetRequest_genericException() {

        when(webClient.get()).thenThrow(new RuntimeException("Connection failed"));

        CaseStudyException ex = assertThrows(CaseStudyException.class,
                                             () -> connection.executeGetRequest("/test", Map.of()));

        assertNotNull(ex);
        verify(span).error(any(RuntimeException.class));
        verify(span).end();
    }
}
