package com.example.microservice.AuthMicroservice.repository;

import com.example.microservice.AuthMicroservice.entity.User;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Observed
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findUserById(String id);
    Optional<User> findUserByEmail(String email);

    @Override
    <S extends User> S save(S entity);

    @Override
    <S extends User> List<S> saveAll(Iterable<S> entities);

    @Override
    List<User> findAll();

    @Override
    void deleteAll();
}
