package com.example.microservice.AuthMicroservice.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Address")
public class Address {

    @MongoId(FieldType.OBJECT_ID)
    private String id;
    private String street;
    private String houseNumber;
    private String city;
    private String zipCode;
    private String description;

}
