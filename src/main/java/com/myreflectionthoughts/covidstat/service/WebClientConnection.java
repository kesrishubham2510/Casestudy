package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class WebClientConnection implements IRemoteConnection<String> {

    private final String hostName;
    private final WebClient webClient;
    private static final Logger logger;

    static {
        logger = Logger.getLogger(WebClientConnection.class.getSimpleName());
    }

    public WebClientConnection() {
        this.hostName = "https://disease.sh";
        this.webClient = WebClient.builder()
                .baseUrl(hostName)
                .build();
    }

    @Override
    public String executeGetRequest(String url, Map<String, String> headers) {

        try {
            logger.info("Invoking... { " + url + " }");

            String result = webClient.get()
                    .uri(URI.create(hostName+url))
                    .headers(httpHeaders -> headers.forEach(httpHeaders::add))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), response ->
                            handleError(response,  ServiceConstant._ERR_BAD_REQUEST_KEY))
                    .onStatus(status -> status.is5xxServerError(), response ->
                            handleError(response, ServiceConstant._ERR_REQUEST_PROCESSING_ERROR_KEY))
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(2000))
                    .block();

            logger.info("Request to 3rd party completed successfully");
            return result;

        } catch (CaseStudyException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Exception occurred, while connecting:- " + e.getMessage());
            throw new CaseStudyException(
                    ServiceConstant._ERR_CONNECT_KEY,
                    "Exception | Something Went Wrong while making 3rd party call to " + hostName
            );
        }
    }

    private Mono<CaseStudyException> handleError(
            ClientResponse response,
            String errorKey
    ) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> {
                    logger.severe("Exception occurred, status code:- " + response.statusCode().value());
                    return new CaseStudyException(
                            errorKey,
                            response.statusCode().value(),
                            body
                    );
                });
    }
}