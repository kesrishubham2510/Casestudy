package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpConnectionTest {

    private final HttpClient httpClient;
    private final HttpConnection httpConnection;
    private final HttpResponse<String> response;


    public HttpConnectionTest() {
        httpClient = mock(HttpClient.class);
        httpConnection = new HttpConnection(httpClient);
        response = mock(HttpResponse.class);
    }

    @Test
    void shouldReturnBodyWhenStatus200() throws Exception {

        Map<String, String> headers = Map.of();
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("SUCCESS");

        when(httpClient.send(
                ArgumentMatchers.any(HttpRequest.class),
                ArgumentMatchers.any(HttpResponse.BodyHandler.class)
        )).thenReturn(response);

        String result = httpConnection.executeGetRequest("/test", headers);

        assertEquals("SUCCESS", result);
    }

    @Test
    void shouldThrowBadRequestExceptionFor4xx() throws Exception {

        Map<String, String> headers = Map.of();
        when(response.statusCode()).thenReturn(404);
        when(response.body()).thenReturn("NOT FOUND");

        when(httpClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(response);

        assertThrows(
                CaseStudyException.class,
                () -> httpConnection.executeGetRequest("/test", headers)
        );

    }

    @Test
    void shouldThrowProcessingErrorFor5xx() throws Exception {

        Map<String, String> headers = Map.of();
        when(response.statusCode()).thenReturn(500);
        when(response.body()).thenReturn("SERVER ERROR");

        when(httpClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(response);

        assertThrows(
                CaseStudyException.class,
                () -> httpConnection.executeGetRequest("/test", headers)
        );

    }

    @Test
    void shouldThrowGenericErrorForNon200Non4xxNon5xx() throws Exception {

        Map<String, String> headers = Map.of();
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(302);
        when(response.body()).thenReturn("REDIRECT");

        when(httpClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(response);

        assertThrows(
                CaseStudyException.class,
                () -> httpConnection.executeGetRequest("/test", headers)
        );

    }

    @Test
    void shouldThrowConnectionErrorOnIOException() throws Exception {

        Map<String, String> headers = Map.of();

        when(httpClient.send(any(), any()))
                .thenThrow(new IOException("Network error"));

        assertThrows(
                CaseStudyException.class,
                () -> httpConnection.executeGetRequest("/test", headers)
        );

    }

    @Test
    void shouldThrowConnectionErrorOnInterruptedException() throws Exception {

        Map<String, String> headers = Map.of();
        when(httpClient.send(any(), any()))
                .thenThrow(new InterruptedException("Interrupted"));

        assertThrows(
                CaseStudyException.class,
                () -> httpConnection.executeGetRequest("/test", headers)
        );

    }
}