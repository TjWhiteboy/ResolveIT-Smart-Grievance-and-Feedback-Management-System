package com.example.resolveit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private com.example.resolveit.service.NotificationService notificationService;

    @Autowired
    private com.example.resolveit.repository.UserRepository userRepository;

    @PostMapping("/markAllRead")
    public ResponseEntity<String> markAllRead(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Unauthorized");
        
        com.example.resolveit.model.User user = userRepository.findByEmail(principal.getName());
        if (user != null) {
            notificationService.markAllAsRead(user.getId());
            return ResponseEntity.ok("Success");
        }
        return ResponseEntity.status(401).body("User not found");
    }
}
