package com.example.microservice.AuthMicroservice.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FavoritesStockResponse {
    private List<String> stockSymbol;
}
