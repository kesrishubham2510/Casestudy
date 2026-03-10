package com.myreflectionthoughts.covidstat.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenAPIConfiguration {

        private static final String API_KEY_NAME = "API-KEY";

        @Bean
        public OpenAPI customOpenAPI() {

            SecurityScheme apiKeyScheme = new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name(API_KEY_NAME);

            return new OpenAPI()
                    .addSecurityItem(new SecurityRequirement().addList(API_KEY_NAME))
                    .components(new Components().addSecuritySchemes(API_KEY_NAME, apiKeyScheme));
        }
    }
