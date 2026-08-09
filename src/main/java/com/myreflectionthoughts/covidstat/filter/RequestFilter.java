package com.myreflectionthoughts.covidstat.filter;

import com.myreflectionthoughts.covidstat.config.APIKeyConfig;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.myreflectionthoughts.covidstat.constant.ServiceConstant.CORRELATION_HEADER;

@Component
public class RequestFilter extends OncePerRequestFilter {
    private String apiKey;
    private final Logger logger = LoggerFactory.getLogger(RequestFilter.class.getSimpleName());

    public RequestFilter(APIKeyConfig apiKeyConfig){
        super();
        this.apiKey = apiKeyConfig.getKey();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        checkAPIKey(request, response, filterChain);
    }

    @Override
    public boolean shouldNotFilter(HttpServletRequest request){
        String requestURI = request.getRequestURI();
        return request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name()) || requestURI.contains("/swagger") || requestURI.contains("/swagger-ui") || requestURI.contains("/v3/api-docs") || requestURI.contains("/swagger-ui.html") || requestURI.contains("/api/covid-stat/health") || requestURI.contains("/api/covid-stat/info");
    }

    private void checkAPIKey(HttpServletRequest request, HttpServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        String requestApiKey = request.getHeader("API-KEY");
        String correlationId = request.getHeader(CORRELATION_HEADER);

        if (StringUtils.isEmpty(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put("requestURI", request.getRequestURI());
        MDC.put("correlationId", correlationId);

        if(StringUtils.isEmpty(requestApiKey)){
            logger.warn("No API Key found");
        }

        if (requestApiKey == null || !requestApiKey.equals(apiKey)) {
            servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            servletResponse.getWriter().write("Invalid API Key");
            return;
        }

        try {
            servletResponse.setHeader(CORRELATION_HEADER, correlationId);
            filterChain.doFilter(request, servletResponse);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("requestURI");
        }
    }
}
