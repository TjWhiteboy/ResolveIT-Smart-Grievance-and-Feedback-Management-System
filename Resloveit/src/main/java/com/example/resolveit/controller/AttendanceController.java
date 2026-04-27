package com.example.resolveit.controller;

import com.example.resolveit.model.Attendance;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.UserRepository;
import com.example.resolveit.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserRepository userRepository;

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userRepository.findByEmail(auth.getName());
        }
        return null;
    }

    @PostMapping("/checkin")
    public String checkIn(RedirectAttributes ra) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login.html";

        if (attendanceService.checkIn(user)) {
            ra.addFlashAttribute("success", "Checked in successfully! Have a great shift.");
        } else {
            ra.addFlashAttribute("error", "You have already checked in for today.");
        }

        return "redirect:/staff/dashboard";
    }

    @PostMapping("/checkout")
    public String checkOut(RedirectAttributes ra) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login.html";

        if (attendanceService.checkOut(user)) {
            ra.addFlashAttribute("success", "Checked out successfully! See you tomorrow.");
        } else {
            Attendance att = attendanceService.getTodayAttendance(user.getId());
            if (att == null) {
                ra.addFlashAttribute("error", "You must check in before you can check out.");
            } else {
                ra.addFlashAttribute("error", "You have already checked out for today.");
            }
        }

        return "redirect:/staff/dashboard";
    }
}
