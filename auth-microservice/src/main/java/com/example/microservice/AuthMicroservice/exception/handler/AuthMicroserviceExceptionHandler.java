package com.example.microservice.AuthMicroservice.exception.handler;

import com.example.microservice.AuthMicroservice.exception.TokenAlreadyExpiredException;
import com.example.microservice.AuthMicroservice.exception.UserExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
@Slf4j
public class AuthMicroserviceExceptionHandler {

    private static final String USER_EXIST = "User already exist";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String TOKEN_ALREADY_EXPIRED = "Token already expired";

    @ExceptionHandler(value={ TokenAlreadyExpiredException.class })
    public ResponseEntity<Object> handleTokenAlreadyExpiredException(
            TokenAlreadyExpiredException tokenAlreadyExpiredException){

        log.error("[handleTokenAlreadyExpiredException]  {}",TOKEN_ALREADY_EXPIRED,
                tokenAlreadyExpiredException);
        return new ResponseEntity<>(TOKEN_ALREADY_EXPIRED, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value={ UserExistException.class })
    public ResponseEntity<Object> handleUserExistException(UserExistException userExistException){

        log.error("[handleUserExistException]  {}", USER_EXIST, userExistException);
        return new ResponseEntity<>(USER_EXIST, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value={ UsernameNotFoundException.class })
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException usernameNotFoundException){

        log.error("[handleUsernameNotFoundException]  {}", USER_NOT_FOUND, usernameNotFoundException);
        return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

}
