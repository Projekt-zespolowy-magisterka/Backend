package com.example.microservice.AuthMicroservice.repository;

import com.example.microservice.AuthMicroservice.entity.Token;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {

    List<Token> findAllByUser_IdAndExpiredFalseAndRevokedFalse(String id);
    List<Token> findAllByUser_Id(String id);
    void deleteAllByExpiredIsTrue();
    Optional<Token> findTokenById(String id);
    Optional<Token> findByToken(String token);
}
