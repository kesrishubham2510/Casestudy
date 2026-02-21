package com.myreflectionthoughts.covidstat.filter;

import com.myreflectionthoughts.covidstat.config.APIKeyConfig;
import com.myreflectionthoughts.covidstat.config.CountryConfig;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestFilter extends OncePerRequestFilter {
    private String apiKey;
    private final CountryConfig countryConfig;

    public RequestFilter(CountryConfig countryConfig, APIKeyConfig apiKeyConfig){
        super();
        this.countryConfig = countryConfig;
        this.apiKey = apiKeyConfig.getKey();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        checkAPIKey(request, response, filterChain);
    }

    @Override
    public boolean shouldNotFilter(HttpServletRequest request){
        String requestURI = request.getRequestURI();
        return requestURI.contains("/swagger") || requestURI.contains("/swagger-ui") || requestURI.contains("/v3/api-docs") || requestURI.contains("/swagger-ui.html");
    }

    private void checkAPIKey(HttpServletRequest request, HttpServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String requestApiKey = request.getHeader("API-KEY");

        if(StringUtils.isEmpty(requestApiKey)){
            System.out.println("No API Key found");
        }

        if (requestApiKey == null || !requestApiKey.equals(apiKey)) {
            servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            servletResponse.getWriter().write("Invalid API Key");
            return;
        }

        filterChain.doFilter(request, servletResponse);
    }
}
