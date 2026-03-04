package com.example.resolveit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.resolveit.model.User;
import com.example.resolveit.repository.UserRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        if(user != null) {
            userRepository.save(user);
        }

        return "User Registered Successfully";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {

        User existing = userRepository.findByEmail(user.getEmail());

        if(existing != null && existing.getPassword().equals(user.getPassword())) {
            return "Login Successful";
        }

        return "Invalid Credentials";
    }
}