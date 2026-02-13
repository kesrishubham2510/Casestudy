package com.myreflectionthoughts.covidstat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cache.ttl")
public class CacheTTLConfig {
    private int latestStatCountry;
    private int oneDayPrevious;
    private int twoDayPrevious;
    private int vaccineCoverageTrends;
    private int alertMessage;
}
