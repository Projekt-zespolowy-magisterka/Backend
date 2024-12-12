package com.example.microservice.AuthMicroservice.service;

import com.example.microservice.AuthMicroservice.entity.User;
import com.example.microservice.AuthMicroservice.entity.enums.Role;
import com.example.microservice.AuthMicroservice.exception.UserExistException;
import com.example.microservice.AuthMicroservice.repository.UserRepository;
import com.example.microservice.AuthMicroservice.request.AddFavoriteStockRequest;
import com.example.microservice.AuthMicroservice.request.AuthRequest;
import com.example.microservice.AuthMicroservice.request.RegistrationRequest;
import com.example.microservice.AuthMicroservice.response.AuthResponse;
import com.example.microservice.AuthMicroservice.response.BasicResponse;
import com.example.microservice.AuthMicroservice.response.FavoritesStockResponse;
import com.example.microservice.AuthMicroservice.response.FoundUserResponse;
import com.example.microservice.AuthMicroservice.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.example.microservice.AuthMicroservice.security.TokenService.TWENTY_MINUTES;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final WebClient.Builder webClientBuilder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    private final static String USER_CREATED = "User created";
    private final static String USER_NOT_FOUND = "User not found";
    private final static String FOUND_USER = "User found";
    private final static String AUTHENTICATED = "Logged in";
    private final static String USER_UPDATED = "Your account has been updated";
    private final static String USER_DELETED = "Your account has been deleted";
    private final static String ORGANIZATION_EMAIL_SUFFIX = "@stock-master.org";

    public FoundUserResponse findUserById(String id) {
        FoundUserResponse foundUserResponse = new FoundUserResponse();
        try {
            User fetchedUser = userRepository.findUserById(id).orElseThrow();
            foundUserResponse.setUser(fetchedUser);
            foundUserResponse.setMessage(FOUND_USER);
            if (log.isDebugEnabled()) {
                log.debug("[findUserById] Found user: {}", fetchedUser);
            }
            return foundUserResponse;
        } catch (NoSuchElementException e) {
            foundUserResponse.setUser(null);
            foundUserResponse.setMessage(USER_NOT_FOUND);
            if (log.isDebugEnabled()) {
                log.debug("[findUserById] User with id: {}, not found", id);
            }
            return foundUserResponse;
        }
    }

    public FoundUserResponse findUserByEmail(String email) {
        FoundUserResponse foundUserResponse = new FoundUserResponse();
        try {
            User fetchedUser = userRepository.findUserByEmail(email).orElseThrow();
            foundUserResponse.setUser(fetchedUser);
            foundUserResponse.setMessage(FOUND_USER);
            if (log.isDebugEnabled()) {
                log.debug("[findUserByEmail] Found user: {}", fetchedUser);
            }
            return foundUserResponse;
        } catch (NoSuchElementException e) {
            foundUserResponse.setUser(null);
            foundUserResponse.setMessage(USER_NOT_FOUND);
            if (log.isDebugEnabled()) {
                log.debug("[findUserByEmail] User with email: {}, not found", email);
            }
            return foundUserResponse;
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

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

    //TODO TO ADJUST
    public BasicResponse editUser(User editUser) {

        BasicResponse basicResponse = new BasicResponse();
        User fetchedUser = userRepository.findUserById(editUser.getId()).orElseThrow();
        String newPassword = editUser.getPassword();
        editUser.setDateCreated(fetchedUser.getDateCreated());
        editUser.setRole(fetchedUser.getRole());
        editUser.setAccountNonExpired(fetchedUser.getAccountNonExpired());
        editUser.setAccountNonLocked(fetchedUser.getAccountNonLocked());
        editUser.setEnabled(fetchedUser.getEnabled());
        editUser.setCredentialsNonExpired(fetchedUser.getCredentialsNonExpired());
        editUser.setAddress(editUser.getAddress());

        if (!existPassword(newPassword)) {
            editUser.setPassword(fetchedUser.getPassword());
        } else {
            editUser.setPassword(passwordEncoder.encode(editUser.getPassword()));
        }
        userRepository.save(editUser);
        basicResponse.setMessage(USER_UPDATED);
        return basicResponse;
    }

    public BasicResponse deleteUser(String id) {
        BasicResponse basicResponse = new BasicResponse();
        userRepository.deleteById(id);
        basicResponse.setMessage(USER_DELETED);
        return basicResponse;
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

    public FavoritesStockResponse addStocksToFavorites(AddFavoriteStockRequest addFavoriteStockRequest) {
        var user = userRepository.findUserById(addFavoriteStockRequest.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User with ID " + addFavoriteStockRequest.getUserId() + " not found"));

        List<String> currentFavorites = user.getFavoritesStocks();
        List<String> newStocks = addFavoriteStockRequest.getStockSymbol();

        List<String> stocksToAdd = newStocks.stream()
                .filter(stock -> !currentFavorites.contains(stock)).toList();

        if (!stocksToAdd.isEmpty()) {
            currentFavorites.addAll(stocksToAdd);
            user.setFavoritesStocks(currentFavorites);
            userRepository.save(user);
        }

        return new FavoritesStockResponse(currentFavorites);
    }

    public FavoritesStockResponse getFavoriteStocks(String id) {
        var user = userRepository.findUserById(id);
        return user.map(value -> new FavoritesStockResponse(value.getFavoritesStocks()))
                .orElseGet(() -> new FavoritesStockResponse(Collections.emptyList()));
    }

    private static boolean userHasOrganizationEmail(String email) {
        return email.contains(ORGANIZATION_EMAIL_SUFFIX);
    }

    private boolean exist(String email) {
        FoundUserResponse foundUserResponse = findUserByEmail(email);
        return foundUserResponse.getUser() != null;
    }

    private static boolean existPassword(String newPassword) {
        return newPassword != null;
    }
}
