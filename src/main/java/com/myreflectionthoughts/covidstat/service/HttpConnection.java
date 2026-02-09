package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.logging.Logger;

public class HttpConnection implements IRemoteConnection<String> {
    private final String hostName;
    private final HttpClient httpClient;
    private static final Logger logger;

    static {
        logger = Logger.getLogger(HttpConnection.class.getSimpleName());
    }

    public HttpConnection(HttpClient httpClient){
        this.hostName = "https://disease.sh";
        this.httpClient = httpClient;
    }

    @Override
    public String executeGetRequest(String url, Map<String, String> headers) {
        HttpRequest httpRequest;

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(hostName+url)).GET();

        headers.forEach(builder::header);
        httpRequest = builder.build();

        HttpResponse<String> httpResponse;

        try {
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            // all kinds of network level connection exception are thrown as IOException
            logger.severe("Exception occurred, while connecting:- "+e.getMessage());
            throw new CaseStudyException("_ERR_CONNECT", "Exception | Something Went Wrong while making 3rd part call to "+hostName);
        }

        if(httpResponse.statusCode()>=400 && httpResponse.statusCode()<500){
            throw new CaseStudyException("BAD_REQUEST", httpResponse.statusCode(), httpResponse.body());
        }else if(httpResponse.statusCode()>=500 && httpResponse.statusCode()<600){
            throw new CaseStudyException("REQUEST_PROCESSING_ERROR", httpResponse.statusCode(), httpResponse.body());
        }else if(httpResponse.statusCode()!=200){
            throw new CaseStudyException("_ERR_COCCURED", httpResponse.statusCode(), httpResponse.body());
        }

        return httpResponse.body();
    }
}
