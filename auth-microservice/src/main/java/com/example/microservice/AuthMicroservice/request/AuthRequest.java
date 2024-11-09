package com.example.microservice.AuthMicroservice.request;

import lombok.Data;

@Data
public class AuthRequest {

    private String email;
    private String password;
}
