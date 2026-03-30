package com.example.resolveit.controller;

import com.example.resolveit.model.User;
import com.example.resolveit.service.UserService;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.ComplaintHistoryRepository;
import com.example.resolveit.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository; // Still needed for existsByEmail check if not in service

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── GET /profile ──────────────────────────────────────────────────────────
    @GetMapping("/profile")
    public String viewProfile(Principal principal, Model model) {
        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("userName", user.getName());
        model.addAttribute("pageTitle", "My Profile");
        return "profile";
    }

    // ── POST /profile/update ──────────────────────────────────────────────────
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam("profilePic") MultipartFile file,
            Principal principal) throws IOException {

        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        // Email uniqueness check (if email changed)
        if (!email.equalsIgnoreCase(user.getEmail())) {
            User existing = userService.findByEmail(email);
            if (existing != null) {
                return "redirect:/profile?error=Email+already+in+use";
            }
        }

        user.setName(name);
        user.setEmail(email);

        // Handle profile picture upload
        if (!file.isEmpty()) {
            // Create uploads directory if it doesn't exist
            Path uploadsDir = Paths.get("uploads");
            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
            }
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path savePath = uploadsDir.resolve(filename);
            Files.write(savePath, file.getBytes());
            user.setProfilePic(filename);
        }

        userService.save(user);
        return "redirect:/profile?success=true";
    }

    // ── POST /profile/change-password ─────────────────────────────────────────
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            Principal principal) {

        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        // Verify current password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return "redirect:/profile?error=Current+password+is+incorrect";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
        return "redirect:/profile?success=Password+changed+successfully";
    }

    // ── POST /profile/delete ──────────────────────────────────────────────────
    @PostMapping("/profile/delete")
    public String deleteAccount(Principal principal, HttpServletResponse response) {
        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        userService.deleteUserWithData(user.getId());
        return "redirect:/login?success=Account+deleted+successfully";
    }
}
