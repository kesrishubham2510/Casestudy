package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.enums.USECASE;
import com.myreflectionthoughts.covidstat.registry.URLTemplateRegistry;
import io.micrometer.common.util.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpConnectionTest {

    private final HttpConnection httpConnection;
    private final URLTemplateRegistry urlTemplateRegistry;

    public HttpConnectionTest(){
        this.httpConnection = new HttpConnection();
        this.urlTemplateRegistry = new URLTemplateRegistry();
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
}

