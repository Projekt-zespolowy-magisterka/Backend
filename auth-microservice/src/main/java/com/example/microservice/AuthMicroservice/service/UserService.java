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
import java.util.function.Function;

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
        return findUser(id, userRepository::findUserById, "[findUserById]");
    }

    public FoundUserResponse findUserByEmail(String email) {
        return findUser(email, userRepository::findUserByEmail, "[findUserByEmail]");
    }

    private FoundUserResponse findUser(String identifier, Function<String, Optional<User>> userFinder, String logPrefix) {
        return userFinder.apply(identifier)
                .map(user -> {
                    if (log.isDebugEnabled()) {
                        log.debug("{} Found user: {}", logPrefix, user);
                    }
                    return new FoundUserResponse(user, FOUND_USER);
                })
                .orElseGet(() -> {
                    if (log.isDebugEnabled()) {
                        log.debug("{} User not found for identifier: {}", logPrefix, identifier);
                    }
                    return new FoundUserResponse(null, USER_NOT_FOUND);
                });
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public BasicResponse editUserByEmail(String email, EditUserRequest editUserRequest) {
        FoundUserResponse foundUser = findUserByEmail(email);
        return editUser(foundUser.getUser(), editUserRequest);
    }

//    TODO Czy napewno chcesz tej funkcji uzywać jesli zrobilas impla??
    public BasicResponse editUserById(String userId, EditUserRequest editUserRequest) {
        FoundUserResponse foundUser = findUserById(userId);
        return editUser(foundUser.getUser(), editUserRequest);
    }

    private BasicResponse editUser(User user, EditUserRequest editUserRequest) {
        throwIfUserNotFound(user);

        User updatedUser = updateUserInformation(user, editUserRequest);
        userRepository.save(updatedUser);

        return new BasicResponse(USER_UPDATED);
    }

    private User updateUserInformation(User user, EditUserRequest editUserRequest) {
        user.setFirstName(editUserRequest.getFirstName());
        user.setLastName(editUserRequest.getLastName());
        user.setPhone(editUserRequest.getPhoneNumber());
        user.setAddress(editUserRequest.getAddress());
        return user;
    }

    public BasicResponse deleteUser(String id) {
        userRepository.deleteById(id);
        return new BasicResponse(USER_DELETED);
    }

    private static void throwIfUserNotFound(User user) {
        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }
    }

//    TODO change password to implement
    private static boolean existPassword(String newPassword) {
        return newPassword != null;
    }
}
