package com.example.microservice.apigateway.config;

import com.example.microservice.apigateway.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig implements org.springframework.cloud.gateway.filter.GatewayFilter {

    private final RouterValidator routerValidator;
    private final TokenService tokenService;

    //HEADERS
    private static final String ORIGIN_HEADER = "Origin";
    private static final String AUTHORIZATION = "Authorization";

    @Bean
    public SecurityWebFilterChain SecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf().disable()
                .cors();
        if (log.isDebugEnabled()) {
            log.debug("[SecurityFilterChain] CONFIG SECURITY FILTER CHAIN");
        }
        return http.build();
    }

    //KEYWORDS
    private static final String BEARER_KEY_WORD = "Bearer ";

    //HEADERS
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ALLOWED_HEADERS = "Origin, X-Requested-With, Content-Type, Accept, Authorization";
    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (log.isDebugEnabled()) {
            log.debug("[filter] TEST IF IT GOES IN: {}", exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders requestHeaders= request.getHeaders();
        HttpHeaders responseHeaders = setResponseHeaders(requestHeaders);
        exchange.getResponse().getHeaders().addAll(responseHeaders);

        if (log.isDebugEnabled()) {
            log.debug("[filter] exchange: {}", exchange);
        }


        if (routerValidator.isSecured.test(request)) {
            if (authMissing(requestHeaders)) {
                if (log.isDebugEnabled()) {
                    log.debug("[filter] AUTH MISSING");
                }
                return onError(response, HttpStatus.UNAUTHORIZED);
            } else {
                final String authHeader = getAuthHeader(requestHeaders);
                if(!isHeaderValid(authHeader)){
                    if (log.isDebugEnabled()) {
                        log.debug("[filter] AUTH HEADER INVALID");
                    }
                    return onError(response, HttpStatus.UNAUTHORIZED);
                } else {
                    final String token = getJwtToken(authHeader);

                    if (tokenService.isTokenExpired(token)) {
                        if (log.isDebugEnabled()) {
                            log.debug("[filter] TOKEN EXPIRED");
                        }
                        return onError(response, HttpStatus.UNAUTHORIZED);
                    }
                }
            }
        }
        return chain.filter(exchange);
    }

    private HttpHeaders setResponseHeaders(HttpHeaders requestHeaders) {
        String origin = getOriginHeader(requestHeaders);
        HttpHeaders responseHeaders = new HttpHeaders();

        responseHeaders.add(ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
        responseHeaders.add(ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);

        if (log.isDebugEnabled()) {
            log.debug("[setAndroidAndAngularResponseHeaders] origin: {}", origin);
            log.debug("[setAndroidAndAngularResponseHeaders] requestHeaders: {}", requestHeaders);
            log.debug("[setAndroidAndAngularResponseHeaders] responseHeaders: {}", responseHeaders);
        }
        return responseHeaders;
    }

    protected String getAuthHeader(HttpHeaders httpHeaders) {
        return httpHeaders.getOrEmpty(AUTHORIZATION).get(0);
    }

    protected String getJwtToken(String authHeader) {
        int tokenStartIndex = BEARER_KEY_WORD.length();
        return authHeader.substring(tokenStartIndex);
    }

    protected String getOriginHeader(HttpHeaders httpHeaders) {
        try{
            if(httpHeaders.containsKey(ORIGIN_HEADER)) {
                String originHeader = httpHeaders.get(ORIGIN_HEADER).get(0);
                if (log.isDebugEnabled()) {
                    log.debug("[getOriginHeader] originHeader: {}", originHeader);
                }
                return originHeader;
            }
            if (log.isDebugEnabled()) {
                log.debug("[getOriginHeader] null");
            }
            return null;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private Mono<Void> onError(ServerHttpResponse response, HttpStatus httpStatus) {
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private boolean authMissing(HttpHeaders httpHeaders) {
        return !httpHeaders.containsKey(AUTHORIZATION);
    }

    protected boolean isHeaderValid(String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_KEY_WORD);
    }
}
