package com.example.resolveit.controller;

import com.example.resolveit.model.Notification;
import com.example.resolveit.model.User;
import com.example.resolveit.service.UserService;
import com.example.resolveit.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        
        return ResponseEntity.ok(notificationService.getLatestNotifications(user.getId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(user.getId())));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        User user = getCurrentUser();
        if (user != null) {
            notificationService.markAllAsRead(user.getId());
        }
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return null;
        }
        return userService.findByEmail(auth.getName());
    }
}
