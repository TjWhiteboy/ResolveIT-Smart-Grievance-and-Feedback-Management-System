package com.example.resolveit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.resolveit.repository.UserRepository;
import com.example.resolveit.service.ComplaintService;
import com.example.resolveit.model.User;
import com.example.resolveit.service.UserService;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.example.resolveit.model.Complaint;

@Controller
public class AdminController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private com.example.resolveit.repository.FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("complaints", complaintService.findAll());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("slaBreaches", complaintService.getGlobalSLABreachesCount());
        return "admin-dashboard";
    }

    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam int id,
            @RequestParam String status,
            @RequestParam(required = false) String note) {
        
        String updatedBy = "Admin";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            updatedBy = auth.getName();
        }

        complaintService.updateStatus(id, status, note != null ? note : "Status updated to " + status, updatedBy);
        
        return "redirect:/admin";
    }

    @PostMapping("/admin/updateRole")
    public String updateRole(@RequestParam int userId,
            @RequestParam String role) {
        var user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setRole(role);
            userRepository.save(user);
        }
        return "redirect:/admin#users";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false, defaultValue = "") String keyword, Model model) {
        if (keyword == null || keyword.isEmpty()) {
            model.addAttribute("complaints", complaintService.findAll());
        } else {
            model.addAttribute("complaints",
                    complaintService.findByTitleContaining(keyword));
        }
        model.addAttribute("users", userRepository.findAll());
        return "admin-dashboard";
    }

    @PostMapping("/assignStaff")
    public String assignStaff(@RequestParam int complaintId,
            @RequestParam int staffId) {
        
        String updatedBy = "Admin";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            updatedBy = auth.getName();
        }

        complaintService.assignStaff(complaintId, staffId, updatedBy);
        
        return "redirect:/admin";
    }
    @Autowired
    private UserService userService;

    @GetMapping("/admin/users")
    public String manageUsers(Model model, @RequestParam(defaultValue = "0") int page) {
        org.springframework.data.domain.Page<User> userPage = userService.getPaginatedUsers(page, 10);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("pageTitle", "Users & Staff");
        return "admin-users";
    }

    @PostMapping("/admin/change-role/{id}")
    public String changeRole(@org.springframework.web.bind.annotation.PathVariable int id, @RequestParam String role) {
        User user = userService.getById(id);
        if (user != null) {
            user.setRole(role);
            userService.save(user);
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/user-complaints")
    public String userComplaints(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String status,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            Model model) {

        if ((keyword == null || keyword.isEmpty()) && (status == null || status.isEmpty())) {
            org.springframework.data.domain.Page<Complaint> complaintPage = complaintService.getPaginatedAllComplaints(page, 10);
            model.addAttribute("complaints", complaintPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", complaintPage.getTotalPages());
        } else {
            List<Complaint> complaints;
            if (keyword != null && !keyword.isEmpty()) {
                complaints = complaintService.search(keyword);
            } else {
                complaints = complaintService.getByStatus(status);
            }
            model.addAttribute("complaints", complaints);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
        }

        List<User> staffList = userService.getByRole("STAFF");
        model.addAttribute("staffList", staffList);
        model.addAttribute("pageTitle", "User Complaints");

        return "admin-user-complaints";
    }

    @PostMapping("/admin/assign/{id}")
    public String assign(@org.springframework.web.bind.annotation.PathVariable int id, @org.springframework.web.bind.annotation.RequestParam int staffId) {
        complaintService.assignStaff(id, staffId, "Admin");
        return "redirect:/admin/user-complaints";
    }

    @PostMapping("/admin/status/{id}")
    public String updateStatus(@org.springframework.web.bind.annotation.PathVariable int id, @org.springframework.web.bind.annotation.RequestParam String status) {
        complaintService.updateStatus(id, status, "Status updated by admin via panel", "Admin");
        return "redirect:/admin/user-complaints";
    }

    @GetMapping("/admin/feedback")
    public String feedbackDashboard(Model model) {
        java.util.List<com.example.resolveit.model.Feedback> feedbackList = feedbackRepository.findAllWithDetails();

        double avgRating = feedbackList.stream()
                .mapToInt(com.example.resolveit.model.Feedback::getRating)
                .average()
                .orElse(0);

        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("totalReviews", feedbackList.size());
        model.addAttribute("pageTitle", "Feedback Analytics");

        return "admin-feedback";
    }
}
