package com.example.resolveit.controller;

import com.example.resolveit.model.User;
import com.example.resolveit.service.AttendanceService;
import com.example.resolveit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserService userService;

    @GetMapping("/staff/attendance")
    public String viewAttendance(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        model.addAttribute("history", attendanceService.getAttendanceForUser(user.getId()));
        model.addAttribute("pageTitle", "My Attendance");
        return "staff-attendance";
    }

    @PostMapping("/staff/attendance/mark")
    public String markAttendance(@RequestParam String status, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        attendanceService.markAttendance(user.getId(), status);
        return "redirect:/staff/attendance?success=Attendance marked as " + status;
    }

    @GetMapping("/admin/attendance")
    public String viewAllAttendance(Model model) {
        model.addAttribute("attendanceList", attendanceService.getAllAttendance());
        model.addAttribute("pageTitle", "Staff Attendance Logs");
        return "admin-attendance";
    }
}
