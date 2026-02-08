package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class HttpConnection implements IRemoteConnection<String> {
    String hostName = "https://disease.sh/v3/covid-19";
    private final HttpClient httpClient;

    public HttpConnection(MappingUtility mappingUtility){
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String executeGetRequest(String url, Map<String, String> headers) {
        hostName += url;
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(hostName)).GET().build();
        HttpResponse<String> httpResponse;
        ExternalAPIResponse externalAPIResponse = null;

        try {
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return httpResponse.body();
    }
}
