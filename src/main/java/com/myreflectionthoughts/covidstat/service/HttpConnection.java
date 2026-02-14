package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.logging.Logger;

@Service
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
            logger.info("Invoking... { "+url+" }");
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            // all kinds of network level connection exception are thrown as IOException
            logger.severe("Exception occurred, while connecting:- "+e.getMessage());
            throw new CaseStudyException(ServiceConstant._ERR_CONNECT_KEY, "Exception | Something Went Wrong while making 3rd part call to "+hostName);
        }

        if(httpResponse.statusCode()>=400 && httpResponse.statusCode()<500){
            logger.severe("Exception occurred, status code:- "+httpResponse.statusCode());
            throw new CaseStudyException(ServiceConstant._ERR_BAD_REQUEST_KEY, httpResponse.statusCode(), httpResponse.body());
        }else if(httpResponse.statusCode()>=500 && httpResponse.statusCode()<600){
            logger.severe("Exception occurred, status code:- "+httpResponse.statusCode());
            throw new CaseStudyException(ServiceConstant._ERR_REQUEST_PROCESSING_ERROR_KEY, httpResponse.statusCode(), httpResponse.body());
        }else if(httpResponse.statusCode()!=200){
            logger.severe("Exception occurred, status code:- "+httpResponse.statusCode());
            throw new CaseStudyException(ServiceConstant._ERR_OCCURRED_KEY, httpResponse.statusCode(), httpResponse.body());
        }

        logger.info("Request to 3rd party completed successfully");
        return httpResponse.body();
    }
}
