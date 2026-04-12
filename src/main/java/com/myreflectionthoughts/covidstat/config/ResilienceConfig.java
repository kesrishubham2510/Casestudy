package com.myreflectionthoughts.covidstat.config;

import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.logging.Logger;


@Component
public class ResilienceConfig {

    private final Logger logger = Logger.getLogger(ResilienceConfig.class.getSimpleName());

    @Bean
    public CircuitBreakerConfig buildCircuitBreakerConfig() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(2000))
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(3)
                .minimumNumberOfCalls(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .slidingWindowSize(10)
                .recordException(exception -> {
                    if (exception instanceof CaseStudyException) {
                        int statusCode = ((CaseStudyException) exception).getStatusCode();
                        return statusCode >= 500;
                    }

                    return true;
                }).build();

        return circuitBreakerConfig;
    }

    @Bean
    public RetryConfig buildRetryConfig() {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .failAfterMaxAttempts(true)
                .waitDuration(Duration.ofMillis(100))
                .retryOnException(exception -> {
                    if (exception instanceof CaseStudyException) {
                        int statusCode = ((CaseStudyException) exception).getStatusCode();
                        return statusCode >= 500;
                    }

                    return true;
                }).build();

        return retryConfig;
    }

    @Bean
    public RetryRegistry buildCustomRetries(RetryConfig retryConfig){
        return RetryRegistry.of(retryConfig);
    }

    @Bean
    public CircuitBreakerRegistry registerCircuitBreakers(CircuitBreakerConfig circuitBreakerConfig){
        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    @Bean
    public Object attachLoggingOnRetries(RetryRegistry retryRegistry) {

        retryRegistry
                .getEventPublisher()
                .onEntryAdded(event -> {
                    event.getAddedEntry().getEventPublisher()
                            .onIgnoredError(ignoredError -> {
                                logger.info("Ignored Retry | event:- " + ignoredError.getName() + " | exception:- " + ignoredError.getLastThrowable().getMessage());
                            })
                            .onRetry(retryEvent -> {
                                logger.info("Event:- " + retryEvent.getName() + " | Retry attempt:- " + retryEvent.getNumberOfRetryAttempts());
                            }).onError(exhaustedAttempts -> {
                                logger.info("Event:- " + exhaustedAttempts.getName() + " | Retry attempts exhausted:- " + exhaustedAttempts.getNumberOfRetryAttempts() + " | Last Exception:- " + exhaustedAttempts.getLastThrowable().getMessage());

                            });
                });

        return new Object();
    }

    @Bean
    public Object attachLoggingOnCircuitBreakers(CircuitBreakerRegistry circuitBreakerRegistry) {

        circuitBreakerRegistry.getEventPublisher()
                .onEntryAdded(event -> {
                    event.getAddedEntry().getEventPublisher()
                            .onStateTransition(e -> {
                                logger.info("CircuitBreaker:- " + e.getCircuitBreakerName() +
                                                    " | State Transition:- " + e.getStateTransition());
                            })
                            .onFailureRateExceeded(e -> {
                                logger.info("CircuitBreaker:- " + e.getCircuitBreakerName() +
                                                    " | Failure rate exceeded");
                            })
                            .onCallNotPermitted(e -> {
                                logger.info("CircuitBreaker:- " + e.getCircuitBreakerName() +
                                                    " | Call not permitted (OPEN state)");
                            });
                });

        return new Object();
    }
}
