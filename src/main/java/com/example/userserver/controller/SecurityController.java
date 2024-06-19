package com.example.userserver.controller;

import com.example.userserver.model.User;
import com.example.userserver.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/security")
public class SecurityController {

    @Autowired
    private SecurityService securityService;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public User registerUser(@RequestBody UserRegistrationDto userDto) {
        return securityService.registerUser(userDto.getUsername(), userDto.getPassword(), userDto.getRoles());
    }

    @GetMapping(value = "/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User getUser(@PathVariable String username) {
        return securityService.findByUsername(username);
    }
}

class UserRegistrationDto {
    private String username;
    private String password;
    private String[] roles;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }
}
