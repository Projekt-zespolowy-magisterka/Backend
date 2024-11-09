package com.example.microservice.AuthMicroservice.response;

import com.example.microservice.AuthMicroservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoundUserResponse {
    @Field
    private User user;
    private String message;
}
