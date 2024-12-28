package com.example.microservice.AuthMicroservice.controller;

import com.example.microservice.AuthMicroservice.request.AddFavoriteStockRequest;
import com.example.microservice.AuthMicroservice.response.FavoritesStockResponse;
import com.example.microservice.AuthMicroservice.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/app/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<FavoritesStockResponse> getFavoritesStocks(@AuthenticationPrincipal UserDetails userDetails) {
        String userName = userDetails.getUsername();
        FavoritesStockResponse response = favoriteService.getFavoriteStocks(userName);
        if (log.isDebugEnabled()) {
            log.debug("[getFavoritesStocks] Favorites response: {}", response);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<FavoritesStockResponse> addStockToFavorites(
            @RequestBody AddFavoriteStockRequest addFavoriteStockRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userName = userDetails.getUsername();
        FavoritesStockResponse response = favoriteService.addStockToFavorites(addFavoriteStockRequest.getStockSymbol().get(0), userName);
        if (log.isDebugEnabled()) {
            log.debug("[addStockToFavorites] Adding stock to favorites response: {}", response);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{stockSymbol}")
    public ResponseEntity<FavoritesStockResponse> removeStockFromFavorites(
            @PathVariable String stockSymbol,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userName = userDetails.getUsername();
        FavoritesStockResponse response = favoriteService.removeStockFromFavorites(stockSymbol, userName);
        if (log.isDebugEnabled()) {
            log.debug("[removeStockFromFavorites] Removing stock from favorites response: {}", response);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

