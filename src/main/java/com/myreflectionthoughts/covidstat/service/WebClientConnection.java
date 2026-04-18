package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

@Service
public class WebClientConnection implements IRemoteConnection<String> {

    private final String hostName;
    private final WebClient webClient;
    private final Tracer tracer;
    private static final Logger logger = LoggerFactory.getLogger(WebClientConnection.class);

    public WebClientConnection(WebClient webClient, Tracer tracer) {
        this.hostName = "https://disease.sh";
        this.webClient = webClient;
        this.tracer = tracer;
    }

    @Override
    public String executeGetRequest(String url, Map<String, String> headers) {
        Span span = tracer.nextSpan()
                .name("webclient.get")
                .tag("component", "webclient")
                .tag("http.method", "GET")
                .tag("http.host", hostName)
                .tag("http.path", url)
                .start();

        try {
            try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
                logger.info("Invoking 3rd party endpoint {}", url);

                String result = webClient.get()
                        .uri(URI.create(hostName + url))
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
            }

        } catch (CaseStudyException e) {
            span.error(e);
            throw e;
        } catch (Exception e) {
            span.error(e);
            logger.error("Exception occurred while connecting to {}", hostName, e);
            throw new CaseStudyException(
                    ServiceConstant._ERR_CONNECT_KEY,
                    "Exception | Something Went Wrong while making 3rd party call to " + hostName
            );
        } finally {
            span.end();
        }
    }

    private Mono<CaseStudyException> handleError(
            ClientResponse response,
            String errorKey
    ) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> {
                    logger.error("Exception occurred, status code: {}", response.statusCode().value());
                    return new CaseStudyException(
                            errorKey,
                            response.statusCode().value(),
                            body
                    );
                });
    }
}
