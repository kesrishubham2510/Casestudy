package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class HttpConnection implements IRemoteConnection<String> {
    String hostName = "https://disease.sh";
    private final HttpClient httpClient;

    public HttpConnection(){
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String executeGetRequest(String url, Map<String, String> headers) {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(hostName+url)).GET().build();
        HttpResponse<String> httpResponse;
        ExternalAPIResponse externalAPIResponse = null;

        // TODO : Throw exception for status code other than 200

        try {
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return httpResponse.body();
    }
}
