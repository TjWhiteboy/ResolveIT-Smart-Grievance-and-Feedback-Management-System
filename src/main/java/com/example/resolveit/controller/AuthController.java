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
    private JwtUtil jwtUtil;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "USER") String role) {

        // Prevent duplicate email registrations
        if (userRepository.findByEmail(email) != null) {
            return "redirect:/login.html?error=Email address already registered";
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        userRepository.save(user);

        return "redirect:/login.html?success=Account created successfully! Please sign in.";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
            @RequestParam String password,
            jakarta.servlet.http.HttpServletResponse response,
            Model model) {

        User user = userRepository.findByEmail(email);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            String token = jwtUtil.generateToken(email);

            // Set JWT in Cookie for standard browser navigation
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("jwtToken", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true); // Secure against XSS
            cookie.setMaxAge(10 * 60 * 60); // 10 hours
            response.addCookie(cookie);

            model.addAttribute("token", token);
            model.addAttribute("userId", user.getId());
            model.addAttribute("userName", user.getName());

            if ("ADMIN".equals(user.getRole()))
                return "redirect:/admin";

            if ("STAFF".equals(user.getRole()))
                return "redirect:/staff/dashboard";

            return "redirect:/my-complaints?userId=" + user.getId() + "&token=" + token;
        }

        return "redirect:/login.html?error=Invalid email or password";
    }

    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpServletResponse response) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("jwtToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/login.html?success=Logged out successfully";
    }
}