package com.myreflectionthoughts.covidstat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.entity.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class HttpConnection implements IRemoteConnection<ExternalAPIResponse> {
    String hostName = "https://disease.sh/v3/covid-19";
    private final HttpClient httpClient;
    private final MappingUtility mappingUtility;

    public HttpConnection(MappingUtility mappingUtility){
        this.httpClient = HttpClient.newHttpClient();
        this.mappingUtility = mappingUtility;
    }

    @Override
    public ExternalAPIResponse executeGetRequest(String url, Map<String, String> headers) {
        hostName += url;
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(hostName)).GET().build();
        HttpResponse<String> httpResponse;
        ExternalAPIResponse externalAPIResponse = null;

        try {
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        try{
            externalAPIResponse = this.mappingUtility.parseToPOJO(httpResponse.toString(), ExternalAPIResponse.class);
        } catch (JsonProcessingException e) {
            // throw a 409 status code exception
        }

        return externalAPIResponse;
    }
}
