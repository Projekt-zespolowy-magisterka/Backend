package com.example.microservice.AuthMicroservice.request;

import lombok.Data;
import java.util.List;

@Data
public class AddFavoriteStockRequest {
    List<String> stockSymbol;
}
