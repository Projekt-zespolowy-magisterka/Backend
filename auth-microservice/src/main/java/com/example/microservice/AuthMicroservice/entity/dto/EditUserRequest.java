package com.example.microservice.AuthMicroservice.entity.dto;

import com.example.microservice.AuthMicroservice.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditUserRequest {

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("phoneNumber")
    private Integer phoneNumber;

    @JsonProperty("address")
    private Address address;
}
