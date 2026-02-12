package com.myreflectionthoughts.covidstat.config;

import com.myreflectionthoughts.covidstat.handler.BadRequestExceptionHandler;
import com.myreflectionthoughts.covidstat.handler.ConnectionExceptionHandler;
import com.myreflectionthoughts.covidstat.handler.DataProcessingExceptionHandler;
import com.myreflectionthoughts.covidstat.handler.GenericExceptionHandler;
import com.myreflectionthoughts.covidstat.service.HttpConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class BeanConfiguration {

    @Bean(name = "httpClient")
    public HttpClient httpClient(){
        return HttpClient.newHttpClient();
    }

    @Bean(name = "httpConnection")
    public HttpConnection httpConnection(){
        return new HttpConnection(httpClient());
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
}
