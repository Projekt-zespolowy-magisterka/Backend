package com.example.microservice.AuthMicroservice.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
@Builder
public class Token {

    @MongoId(FieldType.OBJECT_ID)
    private String id;
    @Indexed(unique = true)
    private String token;
    private boolean expired;
    private boolean revoked;
    Date issuedAt;
    Date expirationTime;
    @DBRef
    private User user;
    @Indexed(unique = true)
    private Token refreshToken;
}
