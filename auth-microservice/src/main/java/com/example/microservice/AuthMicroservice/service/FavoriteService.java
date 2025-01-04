package com.example.microservice.AuthMicroservice.service;

import com.example.microservice.AuthMicroservice.repository.UserRepository;
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

    //TODO jak by o tym pomyśleć to jest to forma edycji użytkownika może by się odwoływać do user service po edit?
    public FavoritesStockResponse addStockToFavorites(String stockSymbol, String userName) {
        var user = userRepository.findUserByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + userName + " not found"));

        List<String> currentFavorites = user.getFavoritesStocks();

        if (!currentFavorites.contains(stockSymbol)) {
            currentFavorites.add(stockSymbol);
            user.setFavoritesStocks(currentFavorites);
            userRepository.save(user);
        }

        return new FavoritesStockResponse(currentFavorites);
    }

    //TODO jak by o tym pomyśleć to jest to forma edycji użytkownika może by się odwoływać do user service po edit?
    public FavoritesStockResponse removeStockFromFavorites(String stockSymbol, String userName) {
        var user = userRepository.findUserByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + userName + " not found"));

        List<String> currentFavorites = user.getFavoritesStocks();

        if (currentFavorites.contains(stockSymbol)) {
            currentFavorites.remove(stockSymbol);
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
