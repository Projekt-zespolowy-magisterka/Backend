package com.example.microservice.AuthMicroservice.security;

import com.example.microservice.AuthMicroservice.entity.Token;
import com.example.microservice.AuthMicroservice.exception.TokenAlreadyExpiredException;
import com.example.microservice.AuthMicroservice.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import static com.example.microservice.AuthMicroservice.security.TokenAuthFilter.getJwtToken;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final TokenAuthFilter tokenAuthFilter;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = tokenAuthFilter.getAuthHeader(request);
        if(!tokenAuthFilter.isHeaderValid(authHeader)){
            return;
        }
        final String jwtToken = getJwtToken(authHeader);
        Token matchedToken = tokenRepository.findByToken(jwtToken).orElseThrow();
        expireToken(matchedToken);
    }

    private void expireToken(Token matchedToken) {
        if(tokenAuthFilter.isTokenActual(matchedToken)){
            var matchedRefreshToken = matchedToken.getRefreshToken();
            matchedToken.setExpired(true);
            matchedToken.setRevoked(true);
            matchedRefreshToken.setExpired(true);
            matchedToken.setRevoked(true);
            tokenRepository.save(matchedToken);
        }
        else {
            throw new TokenAlreadyExpiredException();
        }
    }
}
