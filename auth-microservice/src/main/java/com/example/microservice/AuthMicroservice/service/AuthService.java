package com.example.microservice.AuthMicroservice.service;

import com.example.microservice.AuthMicroservice.entity.User;
import com.example.microservice.AuthMicroservice.entity.enums.Role;
import com.example.microservice.AuthMicroservice.exception.UserExistException;
import com.example.microservice.AuthMicroservice.repository.UserRepository;
import com.example.microservice.AuthMicroservice.request.AuthRequest;
import com.example.microservice.AuthMicroservice.request.RegistrationRequest;
import com.example.microservice.AuthMicroservice.response.AuthResponse;
import com.example.microservice.AuthMicroservice.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static com.example.microservice.AuthMicroservice.security.TokenService.TWENTY_MINUTES;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    private final static String USER_CREATED = "User created";
    private final static String AUTHENTICATED = "Logged in";
    private final static String ORGANIZATION_EMAIL_SUFFIX = "@stock-master.org";

    public AuthResponse registerUser(RegistrationRequest registrationRequest) {
        String email = registrationRequest.getEmail();
        String assignedRole;
        if (exist(email)) {
            throw new UserExistException();
        }
        if (userHasOrganizationEmail(email)) {
            assignedRole = Role.ADMIN.getLabel();
        } else {
            assignedRole = Role.USER.getLabel();
        }

        var user = User.builder()
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .email(registrationRequest.getEmail())
                .phone(registrationRequest.getPhone())
                .address(registrationRequest.getAddress())
                .favoritesStocks(Collections.emptyList())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .role(assignedRole)
                .dateCreated(new Date(System.currentTimeMillis()))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
        var savedUser = userRepository.save(user);
        if (log.isDebugEnabled()) {
            log.debug("[registerUser] Saved user: {}", savedUser);
        }
        String jwtToken = tokenService.generateToken(user, TWENTY_MINUTES);
        String refreshToken = tokenService.generateRefreshToken(user);
        tokenService.saveTokenEntity(savedUser, jwtToken, refreshToken);

        return AuthResponse.builder()
                .token(jwtToken)
                .authMessage(USER_CREATED)
                .id(user.getId())
                .userEmail(user.getEmail())
                .build();
    }


    public AuthResponse authenticate(AuthRequest authRequest) {
        if (log.isTraceEnabled()) {
            log.trace("[authenticate] Before manager");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        if (log.isTraceEnabled()) {
            log.trace("[authenticate] After manager");
        }
        //TODO czy da się tutaj ominąć jakoś to sprawdzanie bo w detailsach już było ono sprawdzane najpewniej
        //TODO TUTAJ BŁĄD JAK NIE MA TAKIEGO USERA A SIE KTOS PROBUJE LOGOWAC DO ZALATANIA
        var user = userRepository.findUserByEmail(authRequest.getEmail()).orElseThrow();
        if (log.isTraceEnabled()) {
            log.trace("[authenticate] After finding in database");
        }
        tokenService.revokeToken(user);
        var jwtToken = tokenService.generateToken(user, TWENTY_MINUTES);
        var refreshToken = tokenService.generateRefreshToken(user);
        tokenService.saveTokenEntity(user, jwtToken, refreshToken);
        return AuthResponse.builder()
                .token(jwtToken)
                .authMessage(AUTHENTICATED)
                .id(user.getId())
                .userEmail(user.getEmail())
                .build();
    }

    private static boolean userHasOrganizationEmail(String email) {
        return email.contains(ORGANIZATION_EMAIL_SUFFIX);
    }

    private boolean exist(String email) {
        Optional<User> fetchedUser = userRepository.findUserByEmail(email);
        return fetchedUser.isPresent();
    }
} 