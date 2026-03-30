package com.example.resolveit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.resolveit.model.User;
import com.example.resolveit.repository.UserRepository;
import com.example.resolveit.config.JwtUtil;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "USER") String role) {

        // Prevent duplicate email registrations
        if (userRepository.findByEmail(email) != null) {
            return "redirect:/login?error=Email address already registered";
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        userRepository.save(user);

        return "redirect:/login?success=Account created successfully! Please sign in.";
    }
}