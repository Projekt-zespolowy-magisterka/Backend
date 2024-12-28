package com.example.microservice.AuthMicroservice.controller;

import com.example.microservice.AuthMicroservice.request.AuthRequest;
import com.example.microservice.AuthMicroservice.request.RegistrationRequest;
import com.example.microservice.AuthMicroservice.response.AuthResponse;
import com.example.microservice.AuthMicroservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse>register(@RequestBody RegistrationRequest registrationRequest){

        AuthResponse addedUser = userService.registerUser(registrationRequest);
        if (log.isDebugEnabled()){
            log.debug("[register] Registered user: {}",addedUser);
        }
        return new ResponseEntity<>(addedUser, HttpStatus.CREATED);
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse>authenticate(@RequestBody AuthRequest authRequest){

        AuthResponse authUser = userService.authenticate(authRequest);
        if (log.isDebugEnabled()){
            log.debug("[authenticate] Authenticated user: {}",authUser);
        }
        return new ResponseEntity<>(authUser, HttpStatus.CREATED);
    }

}
