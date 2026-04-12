package com.myreflectionthoughts.covidstat.config;

import com.myreflectionthoughts.covidstat.contract.IResponsePopulator;
import com.myreflectionthoughts.covidstat.datasource.RemoteDataSource;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.handler.BadRequestExceptionHandler;
import com.myreflectionthoughts.covidstat.handler.ConnectionExceptionHandler;
import com.myreflectionthoughts.covidstat.handler.DataProcessingExceptionHandler;
import com.myreflectionthoughts.covidstat.handler.GenericExceptionHandler;
import com.myreflectionthoughts.covidstat.service.HttpConnection;
import com.myreflectionthoughts.covidstat.service.NDayAverage;
import com.myreflectionthoughts.covidstat.service.impl.cacheservice.CovidStatRawResponseCacheService;
import com.myreflectionthoughts.covidstat.service.impl.cacheservice.RedisAlertMessageCacheService;
import com.myreflectionthoughts.covidstat.service.impl.cacheservice.RedisCacheService;
import com.myreflectionthoughts.covidstat.service.impl.cacheservice.TrendCacheService;
import com.myreflectionthoughts.covidstat.service.impl.populator.AlertMessagePopulator;
import com.myreflectionthoughts.covidstat.service.impl.populator.CountryStatResponsePopulator;
import com.myreflectionthoughts.covidstat.service.impl.populator.CovidStatTrendPopulator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class BeanConfiguration {

    @Bean(name = "httpClient")
    public HttpClient httpClient(){
        return HttpClient.newHttpClient();
    }


    @Bean(name = "badRequestExceptionHandler")
    public BadRequestExceptionHandler badRequestExceptionHandler(){
        return new BadRequestExceptionHandler();
    }

    @Bean(name = "connectionExceptionHandler")
    public ConnectionExceptionHandler connectionExceptionHandler(){
        return new ConnectionExceptionHandler();
    }

    @Bean(name = "dataProcessingExceptionHandler")
    public DataProcessingExceptionHandler dataProcessingExceptionHandler(){
        return new DataProcessingExceptionHandler();
    }

    @Bean(name = "genericExceptionHandler")
    public GenericExceptionHandler genericExceptionHandler(){
        return new GenericExceptionHandler();
    }

    @Bean(name = "currentStatPopulator")
    public IResponsePopulator<String, CovidStatResponse> currentStatsPopulator(CovidStatRawResponseCacheService cacheFacade, RemoteDataSource remoteDataSource){
        return new CountryStatResponsePopulator(cacheFacade, remoteDataSource);
    }

    @Bean(name = "vaccinationTrendsPopulator")
    public IResponsePopulator<String, CovidStatResponse> covidTrendsPopulator(TrendCacheService trendCacheService, RemoteDataSource remoteDataSource, NDayAverage nDayAverage){
        return new CovidStatTrendPopulator(trendCacheService, remoteDataSource, nDayAverage);
    }

    @Bean(name = "alertMessagePopulator")
    public AlertMessagePopulator alertMessagePopulator(RedisAlertMessageCacheService redisAlertMessageCacheService,
                                                                               CovidStatRawResponseCacheService covidStatRawResponseCacheService,
                                                                               RemoteDataSource remoteDataSource
                                                                               ){
        return new AlertMessagePopulator(redisAlertMessageCacheService, covidStatRawResponseCacheService, remoteDataSource);
    }

    @Bean(name="latestStatCacheService")
    public CovidStatRawResponseCacheService buildCovidStatRawResponseCacheService(CacheTTLConfig cacheTTLConfig, RedisCacheService redisCacheService){
        return  new CovidStatRawResponseCacheService(cacheTTLConfig, redisCacheService);
    }

    @Bean(name="trendStatCacheService")
    public TrendCacheService buildTrendCacheService(CacheTTLConfig cacheTTLConfig, RedisCacheService redisCacheService){
        return  new TrendCacheService(cacheTTLConfig, redisCacheService);
    }

    @Bean(name="alertMessageCacheService")
    public RedisAlertMessageCacheService buildRedisAlertMessageCacheService(CacheTTLConfig cacheTTLConfig, RedisCacheService redisCacheService){
        return  new RedisAlertMessageCacheService(cacheTTLConfig, redisCacheService);
    }


}
