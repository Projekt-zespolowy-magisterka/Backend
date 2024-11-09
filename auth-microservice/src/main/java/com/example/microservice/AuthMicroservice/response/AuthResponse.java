package com.example.microservice.AuthMicroservice.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    //TODO Zmienić to na tylko token i message i pobierać id oraz email po zalogowaniu dopiero
    private String token;
    private String userEmail;
    private String id;
    private String authMessage;
}
