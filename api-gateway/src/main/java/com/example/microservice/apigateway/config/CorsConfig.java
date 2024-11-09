package com.example.microservice.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@Slf4j
public class CorsConfig{

    //CORS VARIABLES
    private static final String ANGULAR_ORIGIN = "http://localhost:4200";
    //TODO SET HERE ORIGIN AND MAKE ANGULAR ORIGNS ADJUSTED
    private static final String ANGULAR_ORIGIN2 = "https://our_company_origin";
    private static final String ANGULAR_ORIGIN3 = "http://localhost:4200/";
    private static final String ANDROID_ORIGIN = "https://10.0.2.2";
    protected static final String ANDROID_ORIGIN2 = "https://localhost";
    private static final String CORS_CONFIG_PATH = "/**";

    //HEADERS
    private static final String ORIGIN_HEADER = "Origin";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT = "Accept";
    private static final String AUTHORIZATION = "Authorization";
    private static final String INCLUDE_TOKEN = "Include-Token";
    private static final String ORIGIN_ACCEPT = "Origin,Accept";
    private static final String X_REQUESTED_WITH = "X-Requested-With";
    private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    //METHODS
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String PATCH = "PATCH";
    private static final String DELETE = "DELETE";
    private static final String OPTIONS = "OPTIONS";

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);

        corsConfiguration.setAllowedOrigins(Arrays.asList(ANGULAR_ORIGIN, ANDROID_ORIGIN, ANDROID_ORIGIN2, ANGULAR_ORIGIN2, ANGULAR_ORIGIN3));

        corsConfiguration.setAllowedHeaders(Arrays.asList(ORIGIN_HEADER, ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION,
                INCLUDE_TOKEN, ORIGIN_ACCEPT, X_REQUESTED_WITH, ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_HEADERS));

        corsConfiguration.setExposedHeaders(Arrays.asList(ORIGIN_HEADER, CONTENT_TYPE, ACCEPT, AUTHORIZATION,
                ACCESS_CONTROL_ALLOW_ORIGIN, ACCESS_CONTROL_ALLOW_CREDENTIALS));

        corsConfiguration.setAllowedMethods(Arrays.asList(GET, POST, PUT, PATCH, DELETE, OPTIONS));

        if (log.isInfoEnabled()) {
            log.info("[corsConfigurationSource] Initializing corsConfigurationSource");
        }
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(CORS_CONFIG_PATH, corsConfiguration);
        return source;
    }
}
