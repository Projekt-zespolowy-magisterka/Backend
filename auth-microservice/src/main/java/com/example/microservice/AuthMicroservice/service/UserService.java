package com.example.microservice.AuthMicroservice.service;

import com.example.microservice.AuthMicroservice.entity.User;
import com.example.microservice.AuthMicroservice.entity.dto.EditUserRequest;
import com.example.microservice.AuthMicroservice.repository.UserRepository;
import com.example.microservice.AuthMicroservice.response.BasicResponse;
import com.example.microservice.AuthMicroservice.response.FoundUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private static final String USER_NOT_FOUND = "User not found";
    private static final String FOUND_USER = "User found";
    private static final String USER_UPDATED = "Your account has been updated";
    private static final String USER_DELETED = "Your account has been deleted";

    public FoundUserResponse findUserById(String id) {
        return findUser(userRepository.findUserById(id), id, "ID");
    }

    public FoundUserResponse findUserByEmail(String email) {
        return findUser(userRepository.findUserByEmail(email), email, "email");
    }

    private FoundUserResponse findUser(Optional<User> userOptional, String identifier, String identifierType) {
        FoundUserResponse response = new FoundUserResponse();
        if (userOptional.isPresent()) {
            User fetchedUser = userOptional.get();
            response.setUser(fetchedUser);
            response.setMessage(FOUND_USER);
            log.debug("[findUserBy{}] Found user: {}", identifierType, fetchedUser);
        } else {
            response.setUser(null);
            response.setMessage(USER_NOT_FOUND);
            log.debug("[findUserBy{}] User with {}: {} not found", identifierType, identifierType, identifier);
        }
        return response;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public BasicResponse editUserByEmail(String email, EditUserRequest editUserRequest) {
        return editUserByIdOrEmail(userRepository.findUserByEmail(email), email, editUserRequest, "email");
    }

    public BasicResponse editUser(String userId, EditUserRequest editUserRequest) {
        return editUserByIdOrEmail(userRepository.findUserById(userId), userId, editUserRequest, "ID");
    }

    private BasicResponse editUserByIdOrEmail(Optional<User> userOptional, String identifier, EditUserRequest editUserRequest, String identifierType) {
        User fetchedUser = userOptional.orElseThrow(() ->
                new UsernameNotFoundException("User not found for " + identifierType + ": " + identifier));

        updateUserInformation(fetchedUser, editUserRequest);
        userRepository.save(fetchedUser);

        return createBasicResponse(USER_UPDATED);
    }

    private void updateUserInformation(User user, EditUserRequest editUserRequest) {
        user.setFirstName(editUserRequest.getFirstName());
        user.setLastName(editUserRequest.getLastName());
        user.setPhone(editUserRequest.getPhoneNumber());
        user.setAddress(editUserRequest.getAddress());
    }

    public BasicResponse deleteUser(String id) {
        userRepository.deleteById(id);
        return createBasicResponse(USER_DELETED);
    }

    private BasicResponse createBasicResponse(String message) {
        BasicResponse response = new BasicResponse();
        response.setMessage(message);
        return response;
    }
}
