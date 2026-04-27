package com.example.resolveit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.resolveit.model.Feedback;
import com.example.resolveit.model.User;
import com.example.resolveit.model.Complaint;
import com.example.resolveit.repository.ComplaintHistoryRepository;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.FeedbackRepository;
import com.example.resolveit.repository.NotificationRepository;
import com.example.resolveit.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Controller
public class PageController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintHistoryRepository complaintHistoryRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/home.html")
    public String homeHtml() {
        return "home";
    }

    @GetMapping("/login.html")
    public String loginHtml() {
        return "login";
    }

    @GetMapping("/register.html")
    public String registerHtml() {
        return "register";
    }

    @GetMapping("/complaint.html")
    public String complaintHtml(@RequestParam(required = false) Long user, Model model) {
        if (user != null) {
            com.example.resolveit.model.User u = userRepository.findById(user).orElse(null);
            model.addAttribute("user", u);
            model.addAttribute("userId", user);
        }
        return "complaint";
    }

    @GetMapping("/success.html")
    public String successHtml() {
        return "success";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/complaint")
    public String complaintPage(@RequestParam(required = false) Long user, Model model) {
        if (user != null) {
            com.example.resolveit.model.User u = userRepository.findById(user).orElse(null);
            model.addAttribute("user", u);
            model.addAttribute("userId", user);
        }
        return "complaint";
    }

    @GetMapping("/complaint/{id}")
    public String complaintDetails(@PathVariable Long id,
            @RequestParam(required = false) Long userId,
            Model model) {
        var complaint = complaintRepository.findById(id).orElse(null);
        if (complaint == null) {
            return "redirect:/";
        }
        var history = complaintHistoryRepository.findByComplaintIdOrderByTimestampDesc(id);
        // Reverse so oldest first (timeline top-to-bottom)
        java.util.Collections.reverse(history);
        model.addAttribute("complaint", complaint);
        model.addAttribute("history", history);

        // ── Feedback data ──
        Feedback feedback = feedbackRepository.findByComplaintId(id);
        model.addAttribute("feedback", feedback);
        model.addAttribute("hasFeedback", feedback != null);
        model.addAttribute("isResolved", "Resolved".equals(complaint.getStatus()));

        // Determine dynamic back URL based on authenticated role
        String backUrl = "/my-complaints?userId=" + (userId != null ? userId : complaint.getUserId());
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            com.example.resolveit.model.User currentUser = userRepository.findByEmail(auth.getName());
            if (currentUser != null) {
                if ("ADMIN".equals(currentUser.getRole())) {
                    backUrl = "/admin";
                } else if ("STAFF".equals(currentUser.getRole())) {
                    backUrl = "/staff/dashboard";
                    model.addAttribute("backUrl", backUrl);
                    model.addAttribute("user", currentUser);
                    model.addAttribute("userName", currentUser.getName());
                    model.addAttribute("userId", userId != null ? userId : complaint.getUserId());
                    model.addAttribute("readOnly", true);
                    model.addAttribute("pageTitle", "Complaint Details");
                    return "staff/complaint-detail";
                } else {
                    backUrl = "/my-complaints?userId=" + currentUser.getId();
                }
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getName());
            }
        }
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("userId", userId != null ? userId : complaint.getUserId());
        return "complaint-details";
    }


    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpServletResponse response) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("jwtToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/login.html?logout=true";
    }
}