package com.example.userserver.controller;

import com.example.userserver.model.User;
import com.example.userserver.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Create a new user
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // Get user by ID
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // Get all users
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
