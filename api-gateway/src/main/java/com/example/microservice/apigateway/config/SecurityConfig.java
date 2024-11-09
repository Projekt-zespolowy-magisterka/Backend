package com.example.microservice.apigateway.config;

import com.example.microservice.apigateway.config.RouterValidator;
import com.example.microservice.apigateway.service.TokenService;
//import jakarta.servlet.Filter;
//import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

import java.util.Arrays;
import java.util.Objects;

@Component
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig implements org.springframework.cloud.gateway.filter.GatewayFilter {

    private final RouterValidator routerValidator;
    private final TokenService tokenService;
    private final CorsConfig corsConfig;

    //AUTHORITIES
    private static final String ADMIN_AUTHORITY = "ADMIN";
    private static final String USER_AUTHORITY = "USER";

    //APP URL PATHS
    private static final String ALL_REQUESTS = "/app/**";
    private static final String REGISTER = "/app/user";
    private static final String LOGIN = "/app/user/auth";
    private static final String UPDATE_USER = "/app/user";
    private static final String DELETE_USER = "/app/user/{id}";
    private static final String FIND_USER_BY_ID = "/app/user/{id}";
    private static final String FIND_USER_BY_EMAIL = "/app/user/email/{email}";

    //CORS VARIABLES
    //TODO CHANGE FOR SOME COMPANY ORIGINS AND ADJUST ANGULAR ORIGINS
    private static final String ANGULAR_ORIGIN = "http://localhost:4200";
    private static final String ANGULAR_ORIGIN2 = "https://some_company_";
    private static final String ANGULAR_ORIGIN3 = "http://localhost:4200/";
    private static final String ANDROID_ORIGIN = "https://10.0.2.2";
    protected static final String ANDROID_ORIGIN2 = "https://localhost";
    private static final String CORS_CONFIG_PATH = "/**";

    //HEADERS
    private static final String ORIGIN_HEADER = "Origin";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT = "Accept";
    private static final String AUTHORIZATION = "Authorization";
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
    public SecurityWebFilterChain SecurityFilterChain(ServerHttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .cors()
//                .authorizeExchange().pathMatchers(HttpMethod.GET, FIND_ORDER_BY_TRACKING_CODE, FIND_FREE_BLACKBOXES, FIND_ORDER_BY_ID)
//                .permitAll()
//                .pathMatchers(HttpMethod.POST, REGISTER, MAKE_ORDER, SEND_SAMPLE, LOGIN)
//                .permitAll()
//                .pathMatchers(HttpMethod.PATCH, TAKE_YOUR_ORDER)
//                .permitAll()
//                .pathMatchers(USER_TRANSPORT, DROP_TRANSPORT, FIND_ALL_SENDER_ORDERS, FIND_ALL_RECIPIENT_ORDERS,
//                        FIND_USER_BY_ID, UPDATE_USER, DELETE_USER, FIND_USER_BY_EMAIL)
//                .hasAnyAuthority(ADMIN_AUTHORITY,USER_AUTHORITY)
//                .pathMatchers(HttpMethod.POST, CREATE_TRANSPORT)
//                .hasAnyAuthority(ADMIN_AUTHORITY,USER_AUTHORITY)
//                .pathMatchers(ALL_REQUESTS)
//                .hasAuthority(ADMIN_AUTHORITY)
//                .anyExchange()
//                .authenticated()
                ;
        if (log.isDebugEnabled()) {
            log.debug("[SecurityFilterChain] CONFIG SECURITY FILTER CHAIN");
        }
        return http.build();
    }



    //KEYWORDS
    private static final String BEARER_KEY_WORD = "Bearer ";
//    private static final String AUTHORIZATION = "Authorization";

    //HEADERS
//    private static final String ORIGIN_HEADER = "Origin";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ALLOWED_HEADERS = "Origin, X-Requested-With, Content-Type, Accept, Authorization";
    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS";
//    private static final String ALLOWED_HEADERS = "*";
//    private static final String ALLOWED_METHODS = "*";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (log.isDebugEnabled()) {
            log.debug("[filter] TEST IF IT GOES IN: {}", exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders httpHeaders = request.getHeaders();
//        if(!Objects.equals(request.getURI().getPath(), LOGIN)){
            HttpHeaders setHeaders = setAndroidAndAngularResponseHeaders(httpHeaders);
            exchange.getResponse().getHeaders().addAll(setHeaders);
//            if (log.isDebugEnabled()) {
//                log.debug("[filter] no a login request: {}", request.getURI().getPath());
//            }
//        }
//        if(){

//        }
        if (log.isDebugEnabled()) {
            log.debug("[filter] exchange: {}", exchange);
        }
//        exchange.getResponse().getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
//        exchange.getResponse().getHeaders().add(ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
//        exchange.getResponse().getHeaders().add(ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);


        if (routerValidator.isSecured.test(request)) {
//        if(false){
            if (authMissing(httpHeaders)) {
                if (log.isDebugEnabled()) {
                    log.debug("[filter] AUTH MISSING");
                }
                return onError(response, HttpStatus.UNAUTHORIZED);
            } else {
                final String authHeader = getAuthHeader(httpHeaders);
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

    private HttpHeaders setAndroidAndAngularResponseHeaders(HttpHeaders httpHeaders) {
        String origin = getOriginHeader(httpHeaders);
        HttpHeaders responseHeaders = new HttpHeaders();
        if(origin !=null){
            if (log.isDebugEnabled()) {
                log.debug("[setAndroidAndAngularResponseHeaders] origin TEST: {}",origin);
            }
//            responseHeaders.add(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
//              responseHeaders.add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
//            responseHeaders.addAll(ACCESS_CONTROL_ALLOW_ORIGIN,Arrays.asList(ANGULAR_ORIGIN, ANDROID_ORIGIN,
//                    ANDROID_ORIGIN2, ANGULAR_ORIGIN2, ANGULAR_ORIGIN3));
        }

        responseHeaders.add(ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
        responseHeaders.add(ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
//        responseHeaders.add(ACCESS_CONTROL_ALLOW_METHODS, "*");
//        responseHeaders.add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
//        responseHeaders.add(ACCESS_CONTROL_ALLOW_HEADERS, "*");
        if (log.isDebugEnabled()) {
            log.debug("[setAndroidAndAngularResponseHeaders] origin: {}",origin);
            log.debug("[setAndroidAndAngularResponseHeaders] httpHeaders: {}",httpHeaders);
            log.debug("[setAndroidAndAngularResponseHeaders] responseHeaders: {}",responseHeaders);
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
