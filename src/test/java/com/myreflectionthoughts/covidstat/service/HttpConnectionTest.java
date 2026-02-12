package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import com.myreflectionthoughts.covidstat.enums.USECASE;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import com.myreflectionthoughts.covidstat.registry.URLTemplateRegistry;
import io.micrometer.common.util.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class HttpConnectionTest {

    private final HttpConnection httpConnection;
    private final HttpConnection mockHttpConnection;
    private final HttpClient mockHttpClient;
    private final URLTemplateRegistry urlTemplateRegistry;
    private final HttpResponse httpResponse;

    public HttpConnectionTest(){

        this.httpConnection = new HttpConnection(HttpClient.newHttpClient());
        this.mockHttpClient = Mockito.mock(HttpClient.class);
        this.httpResponse =  Mockito.mock(HttpResponse.class);
        this.urlTemplateRegistry = URLTemplateRegistry.getURLUrlTemplateRegistryInstance();
        this.mockHttpConnection = new HttpConnection(this.mockHttpClient);
    }

    @Test
    public void testExecuteGetRequest(){

        String country = "India";
        String yesterday = "";
        String twoDaysAgo = "";
        String strict = "true";
        String allowNull = "false";

        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);

        url = url.replace("{country}", country);

        if(StringUtils.isBlank(yesterday)){
            url = url.replace("yesterday={yesterday}&", "");
        }

        if(StringUtils.isBlank(twoDaysAgo)){
            url = url.replace("twoDaysAgo={twoDaysAgo}&", "");
        }

        url = url.replace("{strict}", strict);
        url = url.replace("{allowNull}", allowNull);

        Map<String, String> headers = new HashMap<>();
        headers.put("accept", ": application/json");

        String response = this.httpConnection.executeGetRequest(url, headers);
        assertNotNull(response);
    }

    @Test
    public void testExecuteGetRequest_EmptyYesterday(){

        String country = "India";
        String yesterday = "";
        String twoDaysAgo = "true";
        String strict = "true";
        String allowNull = "false";

        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);

        url = url.replace("{country}", country);

        if(StringUtils.isBlank(yesterday)){
            url = url.replace("yesterday={yesterday}&", "");
        }else{
            url = url.replace("{yesterday}", yesterday);
        }

        if(StringUtils.isBlank(twoDaysAgo)){
            url = url.replace("twoDaysAgo={twoDaysAgo}&", "");
        }else{
            url = url.replace("{twoDaysAgo}", twoDaysAgo);
        }

        url = url.replace("{strict}", strict);
        url = url.replace("{allowNull}", allowNull);


        Map<String, String> headers = new HashMap<>();
        headers.put("accept", ": application/json");

        String response = this.httpConnection.executeGetRequest(url, headers);
        assertNotNull(response);
    }

    @Test
    public void testExecuteGetRequest_EmptyTwoDays(){

        String country = "India";
        String yesterday = "true";
        String twoDaysAgo = "";
        String strict = "true";
        String allowNull = "false";

        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);

        url = url.replace("{country}", country);

        if(StringUtils.isBlank(yesterday)){
            url = url.replace("yesterday={yesterday}&", "");
        }else{
            url = url.replace("{yesterday}", yesterday);
        }

        if(StringUtils.isBlank(twoDaysAgo)){
            url = url.replace("twoDaysAgo={twoDaysAgo}&", "");
        }else{
            url = url.replace("{twoDaysAgo}", twoDaysAgo);
        }

        url = url.replace("{strict}", strict);
        url = url.replace("{allowNull}", allowNull);


        Map<String, String> headers = new HashMap<>();
        headers.put("accept", ": application/json");

        String response = this.httpConnection.executeGetRequest(url, headers);
        assertNotNull(response);
    }

    @Test
    public void testExecuteGetRequest_ThrowsCaseStudyException() throws IOException, InterruptedException {

        String country = "India";
        String yesterday = "";
        String twoDaysAgo = "";
        String strict = "true";
        String allowNull = "false";

        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);

        url = url.replace("{country}", country);

        if(StringUtils.isBlank(yesterday)){
            url = url.replace("yesterday={yesterday}&", "");
        }

        if(StringUtils.isBlank(twoDaysAgo)){
            url = url.replace("twoDaysAgo={twoDaysAgo}&", "");
        }

        url = url.replace("{strict}", strict);
        url = url.replace("{allowNull}", allowNull);

        Map<String, String> headers = new HashMap<>();
        headers.put("accept", ": application/json");

        when(mockHttpClient.send(any(HttpRequest.class), any())).thenThrow(new IOException("Test IOException"));

        CaseStudyException exception = assertThrows(CaseStudyException.class, ()-> this.mockHttpConnection.executeGetRequest("", headers));
        assertEquals(ServiceConstant._ERR_CONNECT_KEY, exception.getKey());
        assertEquals(-1, exception.getStatusCode());
    }

    @Test
    public void testExecuteGetRequest_ThrowsCaseStudyException_BadRequest() throws IOException, InterruptedException {


        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);

        Map<String, String> headers = new HashMap<>();
        headers.put("accept", ": application/json");

        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(400);

        CaseStudyException exception = assertThrows(CaseStudyException.class, ()-> this.mockHttpConnection.executeGetRequest("", headers));
        assertEquals("BAD_REQUEST", exception.getKey());

    }

    @Test
    public void testExecuteGetRequest_ThrowsCaseStudyException_RequestProcessingException() throws IOException, InterruptedException {


        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);

        Map<String, String> headers = new HashMap<>();
        headers.put("accept", ": application/json");

        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(500);

        CaseStudyException exception = assertThrows(CaseStudyException.class, ()-> this.mockHttpConnection.executeGetRequest("", headers));
        assertEquals(ServiceConstant._ERR_REQUEST_PROCESSING_ERROR_KEY, exception.getKey());

    }

    @Test
    public void testExecuteGetRequest_ThrowsCaseStudyException_Err() throws IOException, InterruptedException {


        String url = this.urlTemplateRegistry.getURL(USECASE.LATEST_STAT);

        Map<String, String> headers = new HashMap<>();
        headers.put("accept", ": application/json");

        when(mockHttpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(600);

        CaseStudyException exception = assertThrows(CaseStudyException.class, ()-> this.mockHttpConnection.executeGetRequest("", headers));
        assertEquals(ServiceConstant._ERR_OCCURRED_KEY, exception.getKey());

    }

}

