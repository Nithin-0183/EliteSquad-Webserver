package com.example.userserver.service;

import com.example.userserver.model.User;
import com.example.userserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Create a new user
    public Mono<User> createUser(User user) {
        return Mono.fromCallable(() -> userRepository.save(user));
    }

    // Get user by ID
    public Mono<User> getUserById(Long id) {
        return Mono.fromCallable(() -> userRepository.findById(id).orElse(null));
    }

    // Get all users
    public Flux<User> getAllUsers() {
        return Mono.fromCallable(() -> userRepository.findAll())
                   .flatMapMany(Flux::fromIterable);
    }
}
