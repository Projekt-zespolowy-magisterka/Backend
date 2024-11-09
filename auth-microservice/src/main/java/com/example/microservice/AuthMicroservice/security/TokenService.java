package com.example.microservice.AuthMicroservice.security;

import com.example.microservice.AuthMicroservice.entity.Token;
import com.example.microservice.AuthMicroservice.entity.User;
import com.example.microservice.AuthMicroservice.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

import static com.example.microservice.AuthMicroservice.security.TokenAuthFilter.getJwtToken;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final TokenRepository tokenRepository;
    private static final String SECRET_KEY = "+Q21RyKTHcbo4eAdEqRELagt1zNy4SNk/02bzD21WFEDytxyCRrZHwIGjqTFyhDdfuOj+uQu0ejetM7wu3d/iA==";
    public static final int TWENTY_MINUTES = 1000 * 60 * 20;
    public final int FORTY_MINUTES =  1000 * 60 * 40;
    private static final String AUTHORITIES_CLAIM = "authorities";

    public List<Token> findAllTokens() {
        return tokenRepository.findAll();
    }

    public void deleteAllTokens() {
        tokenRepository.deleteAll();
    }

    public List<Token> findAllUserTokens(String userId) {
        return tokenRepository.findAllByUser_Id(userId);
    }

    public Optional<Token> findTokenById(String id) {
        return tokenRepository.findTokenById(id);
    }

    @Transactional
    public String refreshAccessToken(String authorizationHeader) {
        var token = getJwtToken(authorizationHeader);
        var matchedAccessToken = tokenRepository.findByToken(token).orElseThrow();
        var matchedRefreshToken = matchedAccessToken.getRefreshToken();
        var refreshTokenExpired = matchedRefreshToken.isExpired();

        setExpirationOfTokens(matchedAccessToken, matchedRefreshToken);

        matchedAccessToken.setRefreshToken(matchedRefreshToken);
        if(log.isInfoEnabled()){
            log.debug("[refreshAccessToken] expiredToken: {}", matchedAccessToken);
        }
        tokenRepository.save(matchedAccessToken);

        if (refreshTokenExpired){
            return "";
        }
        var matchedUser = matchedAccessToken.getUser();
        return getNewAccessToken(matchedUser);
    }

    public void setExpirationOfTokens(Token matchedAccessToken, Token matchedRefreshToken) {
        matchedAccessToken.setRevoked(true);
        matchedAccessToken.setExpired(true);
        matchedRefreshToken.setRevoked(true);
        matchedRefreshToken.setExpired(true);
    }

    public String getNewAccessToken(User matchedUser) {
        var newAccessToken = generateToken(matchedUser, TWENTY_MINUTES);
        var newRefreshToken = generateRefreshToken(matchedUser);
        saveTokenEntity(matchedUser, newAccessToken, newRefreshToken);
        return newAccessToken;
    }

    public void deleteAllExpiredToken() {
        tokenRepository.deleteAllByExpiredIsTrue();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    public boolean hasClaim(String token, String claimName) {
        final Claims claims = extractAllClaims(token);
        return claims.get(claimName) != null;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @PostConstruct
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails, int expirationTime) {
        return generateToken(new HashMap<>(), userDetails, expirationTime);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, int expirationTime) {

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .claim(AUTHORITIES_CLAIM, userDetails.getAuthorities())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, FORTY_MINUTES);
    }

    @Transactional
    public void revokeToken(User user) {

        var validTokens = tokenRepository.findAllByUser_IdAndExpiredFalseAndRevokedFalse(user.getId());

        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t -> {
            var refreshToken = t.getRefreshToken();
            setExpirationOfTokens(t, refreshToken);
        });

        tokenRepository.saveAll(validTokens);
    }

    @Transactional
    public void saveTokenEntity(User savedUser, String jwtToken, String refreshToken) {
        var refreshTokenEntity = Token.builder()
                .token(refreshToken)
                .expired(false)
                .revoked(false)
                .user(savedUser)
                .issuedAt(extractIssuedAt(refreshToken))
                .expirationTime(extractExpiration(refreshToken))
                .build();

        var tokenEntity = Token.builder()
                .token(jwtToken)
                .refreshToken(refreshTokenEntity)
                .expired(false)
                .revoked(false)
                .user(savedUser)
                .issuedAt(extractIssuedAt(jwtToken))
                .expirationTime(extractExpiration(jwtToken))
                .build();
        tokenRepository.save(tokenEntity);
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));

    }
}
