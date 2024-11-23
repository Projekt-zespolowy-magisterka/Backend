package com.example.microservice.AuthMicroservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final TokenAuthFilter tokenAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    //AUTHORITIES
    private static final String ADMIN_AUTHORITY = "ADMIN";
    private static final String USER_AUTHORITY = "USER";

    //APP URL PATHS
    private static final String REGISTER = "/app/user/register";
    private static final String LOGIN = "/app/user/auth";
    private static final String STOCK_DATA = "/predictor/data/saved";
    private static final String STOCK_PREDICTION_DATA = "/predictor/prediction";

    //AUTHORIZED
    private static final String LOGOUT_PATH = "/app/user/auth/logout";
    private static final String ALL_REQUESTS = "/app/**";
    private static final String UPDATE_USER = "/app/user";
    private static final String DELETE_USER = "/app/user/{id}";
    private static final String FIND_USER_BY_ID = "/app/user/{id}";
    private static final String FIND_USER_BY_EMAIL = "/app/user/email/**";
    //TODO PUTTING IT PUBLIC CHANGE TO RESTRICTED AFTER FINISH
    private static final String SWAGGER_API = "/swagger-ui.html";
    private static final String PROMETHEUS = "/actuator/prometheus";

    @Bean
    public SecurityFilterChain SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors().disable()
                .authorizeHttpRequests(authorize ->
//                                authorize.requestMatchers("**").permitAll()
                                authorize.requestMatchers(PROMETHEUS)
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, SWAGGER_API, STOCK_DATA)
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, REGISTER, LOGIN)
                                        .permitAll()
                                        .requestMatchers(FIND_USER_BY_ID, UPDATE_USER, DELETE_USER, STOCK_PREDICTION_DATA)
                                        .hasAnyAuthority(ADMIN_AUTHORITY, USER_AUTHORITY)
                                        .requestMatchers(ALL_REQUESTS, FIND_USER_BY_EMAIL)
                                        .hasAuthority(ADMIN_AUTHORITY)
                                        .anyRequest()
                                        .authenticated()
                )
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider).addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout.logoutUrl(LOGOUT_PATH).addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );
        return http.build();
    }
}
