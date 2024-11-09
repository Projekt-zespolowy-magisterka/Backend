package com.example.microservice.AuthMicroservice.controller;

import com.example.microservice.AuthMicroservice.entity.Token;
import com.example.microservice.AuthMicroservice.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final TokenService tokenService;
    private static final String TOKEN_NOT_FOUND_MESSAGE = "Token not found";

    @GetMapping("token/{id}")
    public ResponseEntity<Token> getTokenById(@PathVariable("id") String id){

        Optional<Token> searchedToken = tokenService.findTokenById(id);
        if (log.isDebugEnabled()){
            log.debug("[getTokenById] Founded token: {}" ,searchedToken);
        }
        return new ResponseEntity<>(searchedToken.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, TOKEN_NOT_FOUND_MESSAGE)), HttpStatus.OK);
    }

    @GetMapping("token")
    public ResponseEntity<List<Token>> getAllTokens(){

        List<Token> searchedTokens = tokenService.findAllTokens();
        if (log.isDebugEnabled()){
            log.debug("[getAllTokens] Founded tokens: {}" ,searchedTokens);
        }
        return new ResponseEntity<>(searchedTokens , HttpStatus.OK);
    }

    @GetMapping("token/user/{id}")
    public ResponseEntity<List<Token>> getAllUserTokens(@PathVariable("id") String id){

        List<Token> searchedTokens = tokenService.findAllUserTokens(id);
        if (log.isDebugEnabled()){
            log.debug("[getAllUserTokens] Founded tokens of user id: {}, tokens: {}" ,id, searchedTokens);
        }
        return new ResponseEntity<>(searchedTokens , HttpStatus.OK);
    }

    @GetMapping("token/refresh")
    public ResponseEntity<String> getRefreshedAccessToken(@RequestHeader("Authorization") String authorizationHeader){
        String refreshedToken = tokenService.refreshAccessToken(authorizationHeader);
        if (log.isDebugEnabled()){
            log.debug("[getRefreshedAccessToken] Refreshed token: {}", refreshedToken);
        }
        return new ResponseEntity<>(refreshedToken, HttpStatus.OK);
    }

    @DeleteMapping("token/expired")
    public void deleteAllExpiredTokens(){
        tokenService.deleteAllExpiredToken();
        if (log.isDebugEnabled()){
            log.debug("[deleteAllExpiredTokens] All expired tokens were deleted");
        }
    }

    @DeleteMapping("tokens")
    public void deleteAllTokens(){
        tokenService.deleteAllTokens();
        if (log.isDebugEnabled()){
            log.debug("[deleteAllTokens] All tokens were deleted");
        }
    }
}
