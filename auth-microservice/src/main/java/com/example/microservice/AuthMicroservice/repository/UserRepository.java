package com.example.microservice.AuthMicroservice.repository;

import com.example.microservice.AuthMicroservice.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findUserById(String id);
    Optional<User> findUserByEmail(String email);

}
