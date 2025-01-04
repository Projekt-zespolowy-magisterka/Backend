package com.example.microservice.apigateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

@Service
public class RouterValidator {

    private static final String REGISTER = "/app/register";
    private static final String LOGIN = "/app/auth";
    private static final String STOCK_DATA = "/predictor/data/saved";

    public static final List<String> openEndpoints = List.of(
            REGISTER, LOGIN, STOCK_DATA
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openEndpoints.stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
