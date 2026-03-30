package com.example.resolveit.controller;

import com.example.resolveit.model.User;
import com.example.resolveit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.resolveit.service.NotificationService notificationService;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User user = userRepository.findByEmail(auth.getName());
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("userName", user.getName());
                model.addAttribute("userRole", user.getRole());
                model.addAttribute("profilePic", user.getProfilePic());
                
                // Add notification data
                model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getId()));
                model.addAttribute("notifications", notificationService.getNotifications(user.getId()));
            }
        }
    }
}
