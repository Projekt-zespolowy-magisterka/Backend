package com.example.microservice.apigateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    @Value("${routes.auth.uri}")
    private String authUri;

    @Value("${routes.auth.path}")
    private String authPath;

    @Value("${routes.prediction.uri}")
    private String predictionUri;

    @Value("${routes.prediction.path}")
    private String predictionPath;

    private final SecurityConfig securityConfig;

    //AUTH MC
    private static final String AUTH_MC_ID = "auth-mc";

    //PREDICTION MC
    private static final String PREDICTION_MC_ID = "prediction-mc";


    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(AUTH_MC_ID, r -> configureRoute(r, authPath, authUri))
                .route(PREDICTION_MC_ID, r -> configureRoute(r, predictionPath, predictionUri))
                .build();
    }

    private Buildable<Route> configureRoute(PredicateSpec r, String path, String uri) {
        log.info("[configureRoute] path: {}, uri: {}", path, uri);

        return r.path(path)
                .filters(f -> f
                        .circuitBreaker(config -> config
                                .setName("authCircuitBreaker")
                                .setFallbackUri("forward:/fallback"))
                        .filter(securityConfig))
                .uri(uri);
    }
}
