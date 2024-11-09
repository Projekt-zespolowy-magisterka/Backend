package com.example.microservice.AuthMicroservice.controller;

import com.example.microservice.AuthMicroservice.entity.User;
import com.example.microservice.AuthMicroservice.request.AuthRequest;
import com.example.microservice.AuthMicroservice.request.RegistrationRequest;
import com.example.microservice.AuthMicroservice.response.AuthResponse;
import com.example.microservice.AuthMicroservice.response.BasicResponse;
import com.example.microservice.AuthMicroservice.response.FoundUserResponse;
import com.example.microservice.AuthMicroservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/user")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService userService;

    @GetMapping("/{id}")
    public ResponseEntity<FoundUserResponse> getUserById(@PathVariable("id") String id){

        FoundUserResponse foundUserResponse = userService.findUserById(id);
        if (log.isDebugEnabled()){
            log.debug("[getUserById] FoundUserResponse: {}",foundUserResponse);
        }
        return new ResponseEntity<>(foundUserResponse, HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<FoundUserResponse> getUserByEmail(@PathVariable("email") String email){

        FoundUserResponse foundUserResponse = userService.findUserByEmail(email);
        if (log.isDebugEnabled()){
            log.debug("[getUserByEmail] FoundUserResponse: {}",foundUserResponse);
        }
        return new ResponseEntity<>(foundUserResponse, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<User>> getAllUsers(){

        List<User> searchedUsers = userService.getAllUsers();
        if (log.isDebugEnabled()){
            log.debug("[getAllUsers] All users: {}",searchedUsers);
        }
        return new ResponseEntity<>(searchedUsers, HttpStatus.OK);
    }

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

    @PatchMapping("")
    public ResponseEntity<BasicResponse>editUser(@RequestBody User editUser){
        BasicResponse basicResponse = userService.editUser(editUser);
        if (log.isDebugEnabled()){
            log.debug("[editUser] Edited user response: {}",basicResponse);
        }
        return new ResponseEntity<>(basicResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BasicResponse>deleteUser(@PathVariable String id){
        BasicResponse basicResponse = userService.deleteUser(id);
        if (log.isDebugEnabled()){
            log.debug("[deleteUser] User with id: {} deleted with response: {}",id,basicResponse);
        }
        return new ResponseEntity<>(basicResponse, HttpStatus.OK);
    }
}
