package com.example.microservice.AuthMicroservice.request;

import com.example.microservice.AuthMicroservice.entity.Address;
import lombok.Data;

@Data
public class RegistrationRequest {
    private String firstName;
    private String lastName;
    private String email;
    private Integer phone;
    private Address address;
    private String password;
}
