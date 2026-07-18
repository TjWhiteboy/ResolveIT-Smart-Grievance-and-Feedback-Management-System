package com.example.resolveit.controller;

import com.example.resolveit.model.User;
import com.example.resolveit.model.Attendance;
import com.example.resolveit.repository.AttendanceRepository;
import com.example.resolveit.service.UserService;
import com.example.resolveit.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping
    public String profile(Model model, java.security.Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("userName", user.getName());
        model.addAttribute("unreadNotifications", notificationService.getUnreadCount(user.getId()));

        // Populate attendance state so the staff navbar widget renders correctly
        if ("STAFF".equals(user.getRole())) {
            LocalDate today = LocalDate.now();
            Attendance todayAtt = attendanceRepository.findByUserIdAndDate(user.getId(), today);
            model.addAttribute("isCheckedIn", todayAtt != null && todayAtt.getCheckOutTime() == null);
            model.addAttribute("hasCheckedOut", todayAtt != null && todayAtt.getCheckOutTime() != null);
        } else {
            model.addAttribute("isCheckedIn", false);
            model.addAttribute("hasCheckedOut", false);
        }
        
        // Calculate profile completion
        int completion = 0;
        if (user.getName() != null && !user.getName().isEmpty()) completion += 20;
        if (user.getEmail() != null && !user.getEmail().isEmpty()) completion += 20;
        if (user.getPhone() != null && !user.getPhone().isEmpty()) completion += 15;
        if (user.getAddress() != null && !user.getAddress().isEmpty()) completion += 15;
        if (user.getBio() != null && !user.getBio().isEmpty()) completion += 15;
        if (user.getProfilePicturePath() != null) completion += 15;
        model.addAttribute("completion", Math.min(completion, 100));
        model.addAttribute("pageTitle", "STAFF".equals(user.getRole()) ? "Staff Dashboard" : "User Dashboard");
        model.addAttribute("userRole", user.getRole());

        return "common/profile";
    }

    @GetMapping("/debug")
    @ResponseBody
    public String debugProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "Auth Context is completely NULL";
        if (!auth.isAuthenticated()) return "Auth is NOT authenticated";
        if (auth.getName().equals("anonymousUser")) return "Auth is anonymousUser";
        
        User user = userService.findByEmail(auth.getName());
        if (user == null) return "User retrieved from DB is NULL for email: " + auth.getName();
        
        return "SUCCESS! Found user: " + user.getEmail() + " as Role: " + user.getRole();
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String name, 
                               @RequestParam String phone,
                               @RequestParam String address,
                               @RequestParam String bio,
                               RedirectAttributes ra) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        try {
            userService.updateProfile(user.getId(), name, phone, address, bio);
            ra.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/settings")
    public String updateSettings(@RequestParam(required = false) boolean emailAlerts,
                                @RequestParam(required = false) boolean browserNotifications,
                                @RequestParam(required = false) boolean darkMode,
                                @RequestParam(required = false) boolean compactSidebar,
                                RedirectAttributes ra) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        userService.updatePreferences(user.getId(), emailAlerts, browserNotifications, darkMode, compactSidebar);
        ra.addFlashAttribute("success", "Settings updated successfully!");
        return "redirect:/profile?tab=settings";
    }

    @PostMapping("/security")
    public String updateSecurity(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes ra) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "New passwords do not match!");
            return "redirect:/profile?tab=security";
        }

        boolean success = userService.updatePassword(user.getId(), currentPassword, newPassword);
        if (success) {
            ra.addFlashAttribute("success", "Password changed successfully!");
        } else {
            ra.addFlashAttribute("error", "Current password is incorrect.");
        }
        return "redirect:/profile?tab=security";
    }

    @PostMapping("/delete")
    public String deleteAccount(RedirectAttributes ra) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        userService.deleteAccount(user.getId());
        SecurityContextHolder.clearContext();
        ra.addFlashAttribute("info", "Your account has been permanently deleted.");
        return "redirect:/login?deleted=true";
    }

    @PostMapping("/upload-pfp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadPfp(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        User user = getCurrentUser();
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "Session expired");
            return ResponseEntity.status(401).body(response);
        }

        try {
            String newPath = userService.updateProfilePicture(user.getId(), file);
            response.put("success", true);
            response.put("path", newPath + "?v=" + System.currentTimeMillis());
            response.put("message", "Profile photo updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return null;
        }
        return userService.findByEmail(auth.getName());
    }
}
