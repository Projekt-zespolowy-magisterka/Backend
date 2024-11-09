package com.example.microservice.AuthMicroservice.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    ADMIN("ADMIN"),
    USER("USER"),
    NOT_REGISTERED("NOT_REGISTERED");

    private final String label;
}
