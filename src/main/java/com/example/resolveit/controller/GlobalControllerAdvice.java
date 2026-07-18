package com.example.resolveit.controller;

import com.example.resolveit.model.User;
import com.example.resolveit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserService userService;

    @Autowired
    private com.example.resolveit.service.NotificationService notificationService;

    @Autowired
    private com.example.resolveit.repository.AttendanceRepository attendanceRepository;

    @ModelAttribute
    public void addUser(Model model, java.security.Principal principal) {
        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("userName", user.getName());
                model.addAttribute("userId", user.getId());
                model.addAttribute("unreadCount", notificationService.getUnreadCount(user.getId()));

                // Provide attendance status globally for the topbar widget
                if ("STAFF".equals(user.getRole())) {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    com.example.resolveit.model.Attendance todayAtt = attendanceRepository.findByUserIdAndDate(user.getId(), today);
                    model.addAttribute("isCheckedIn", todayAtt != null && todayAtt.getCheckOutTime() == null);
                    model.addAttribute("hasCheckedOut", todayAtt != null && todayAtt.getCheckOutTime() != null);
                }
            }
        }
    }
}
