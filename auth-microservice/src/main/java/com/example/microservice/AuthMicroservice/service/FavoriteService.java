package com.example.microservice.AuthMicroservice.service;

import com.example.microservice.AuthMicroservice.repository.UserRepository;
import com.example.microservice.AuthMicroservice.request.AddFavoriteStockRequest;
import com.example.microservice.AuthMicroservice.response.FavoritesStockResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final UserRepository userRepository;

    public FavoritesStockResponse addStocksToFavorites(AddFavoriteStockRequest addFavoriteStockRequest, String userName) {
        var user = userRepository.findUserByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User with ID " + userName + " not found"));

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


    public FavoritesStockResponse getFavoriteStocks(String email) {
        var user = userRepository.findUserByEmail(email);
        return user.map(value -> new FavoritesStockResponse(value.getFavoritesStocks()))
                .orElseGet(() -> new FavoritesStockResponse(Collections.emptyList()));
    }

}
