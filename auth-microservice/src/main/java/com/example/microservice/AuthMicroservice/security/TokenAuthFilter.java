package com.example.microservice.AuthMicroservice.security;

import com.example.microservice.AuthMicroservice.entity.Token;
import com.example.microservice.AuthMicroservice.repository.TokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenAuthFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final TokenService tokenService;
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_KEY_WORD = "Bearer ";
    private static final String ORIGIN = "Origin";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ALLOWED_HEADERS = "Origin, X-Requested-With, Content-Type, Accept, Authorization";
    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

//        setResponseHeaders(request, response);

        final String authHeader = getAuthHeader(request);
        if(!isHeaderValid(authHeader)){
            if(log.isTraceEnabled()){
                log.trace("Header invalid");
            }
            filterChain.doFilter(request, response);
            return;
        }
        final String jwtToken = getJwtToken(authHeader);
        String userEmail = null;
        Token tokenToCheck;
        //TODO ADD PAIR TUPLE RETURN
        try{
            userEmail = tokenService.extractUsername(jwtToken);
        }
        catch (ExpiredJwtException e){
            tokenToCheck = tokenRepository.findByToken(jwtToken).orElseThrow();
            var refreshToken = tokenToCheck.getRefreshToken();
            tokenService.setExpirationOfTokens(tokenToCheck, refreshToken);
            if(log.isInfoEnabled()){
                log.debug("[doFilterInternal] expiredToken1: {}", tokenToCheck);
                log.debug("[doFilterInternal] expiredToken2: {}", refreshToken);
            }
            tokenRepository.save(tokenToCheck);
            filterChain.doFilter(request,response);
        }
        catch (NoSuchElementException e){
            filterChain.doFilter(request,response);
        }

        if(isValidAuthorizationTry(userEmail)) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            //TODO po co tutaj jest to sprawdzanie skoro wyżej już to było sprawdzane (sprawdzic)
            tokenToCheck = tokenRepository.findByToken(jwtToken).orElseThrow();

            if(isTokenValid(jwtToken, userDetails, tokenToCheck)){

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            filterChain.doFilter(request,response);
        }
    }

    private static void setResponseHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader(ORIGIN);
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
        response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
    }

    protected String getAuthHeader(HttpServletRequest request) {
        return request.getHeader(AUTH_HEADER);
    }

    public static String getJwtToken(String authHeader) {
        int tokenStartIndex = BEARER_KEY_WORD.length();
        return authHeader.substring(tokenStartIndex);
    }

    private boolean isValidAuthorizationTry(String userEmail) {
        var authorized = SecurityContextHolder.getContext().getAuthentication();
        return userEmail != null && authorized == null;
    }

    protected boolean isHeaderValid(String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_KEY_WORD);
    }

    private boolean isTokenValid(String jwtToken, UserDetails userDetails, Token tokenToCheck) {
        return tokenService.isTokenValid(jwtToken, userDetails) && isTokenActual(tokenToCheck);
    }

    protected boolean isTokenActual(Token tokenToMatch){
        return !tokenToMatch.isRevoked() && !tokenToMatch.isExpired();
    }
}
