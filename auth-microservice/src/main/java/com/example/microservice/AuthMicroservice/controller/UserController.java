package com.example.microservice.AuthMicroservice.controller;

import com.example.microservice.AuthMicroservice.entity.User;
import com.example.microservice.AuthMicroservice.entity.dto.EditUserRequest;
import com.example.microservice.AuthMicroservice.response.BasicResponse;
import com.example.microservice.AuthMicroservice.response.FoundUserResponse;
import com.example.microservice.AuthMicroservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/app/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<FoundUserResponse> getMyInfo(Principal principal) {
        String email = principal.getName();
        FoundUserResponse foundUserResponse = userService.findUserByEmail(email);
        if (log.isDebugEnabled()) {
            log.debug("[getMyInfo] FoundUserResponse: {}", foundUserResponse);
        }
        return new ResponseEntity<>(foundUserResponse, HttpStatus.OK);
    }

    @PatchMapping("/me")
    public ResponseEntity<BasicResponse> editMyInfo(@RequestBody EditUserRequest editUserRequest, Principal principal) {
        log.debug("Received payload: {}", editUserRequest);
        String email = principal.getName();
        BasicResponse basicResponse = userService.editUserByEmail(email, editUserRequest);
        if (log.isDebugEnabled()) {
            log.debug("[editMyInfo] Edited user response: {}", basicResponse);
        }
        return new ResponseEntity<>(basicResponse, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> allUsers = userService.getAllUsers();
        if (log.isDebugEnabled()) {
            log.debug("[getAllUsers] All users: {}", allUsers);
        }
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<BasicResponse>deleteUser(@PathVariable String id){
        BasicResponse basicResponse = userService.deleteUser(id);
        if (log.isDebugEnabled()){
            log.debug("[deleteUser] User with id: {} deleted with response: {}",id,basicResponse);
        }
        return new ResponseEntity<>(basicResponse, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<BasicResponse>editUser(@PathVariable String id, @RequestBody EditUserRequest editUserRequest){
        BasicResponse basicResponse = userService.editUser(id, editUserRequest);
        if (log.isDebugEnabled()){
            log.debug("[editUser] Edited user response: {}",basicResponse);
        }
        return new ResponseEntity<>(basicResponse, HttpStatus.OK);
    }


}
