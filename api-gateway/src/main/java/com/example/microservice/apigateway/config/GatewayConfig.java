package com.example.microservice.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final SecurityConfig securityConfig;

    //AUTH MC
    private static final String AUTH_MC_ID = "auth-mc";
    private static final String AUTH_PATH = "/app/**";
    private static final String AUTH_MC_URI = "http://localhost:8081";

    //PREDICTION MC
    private static final String PREDICTION_MC_ID = "prediction-mc";
    private static final String PREDICTION_PATH = "/predictor/**";
    private static final String PREDICTION_MC_URI = "http://localhost:5000";


    //DOCKER URI-S
    private static final String PREDICTION_MC_DOCKER_ID = "prediction-mc-docker";
    private static final String PREDICTION_MC_DOCKER_URI = "http://prediction-mc-container:5000";

    private static final String AUTH_MC_DOCKER_ID = "auth-mc-docker";
    private static final String AUTH_MC_DOCKER_URI = "http://auth-mc-container:8081";

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()

                .route(AUTH_MC_ID, r -> configureRoute(r, AUTH_PATH, AUTH_MC_URI))
                .route(PREDICTION_MC_ID, r -> configureRoute(r, PREDICTION_PATH, PREDICTION_MC_URI))

//                //Docker URI/s TODO change all aplications for profiles
//                .route(PREDICTION_MC_DOCKER_ID, r -> configureRoute(r, PREDICTION_PATH, PREDICTION_MC_DOCKER_URI))
//                .route(AUTH_MC_DOCKER_ID, r -> configureRoute(r, AUTH_PATH, AUTH_MC_DOCKER_URI))
                .build();
    }

    private Buildable<Route> configureRoute(PredicateSpec r, String path, String uri) {
        return r.path(path)
                .filters(f -> f
//                        .circuitBreaker(config -> config
//                                .setName("authCircuitBreaker")
//                                .setFallbackUri("forward:/fallback"))
                        .filter(securityConfig))
                .uri(uri);
    }
}
