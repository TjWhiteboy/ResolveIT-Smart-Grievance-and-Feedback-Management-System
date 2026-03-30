package com.example.resolveit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class PageController {

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.resolveit.repository.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.resolveit.service.ComplaintService complaintService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.resolveit.service.ComplaintHistoryService historyService;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(org.springframework.ui.Model model, java.security.Principal principal) {
        if (principal == null) return "redirect:/login";
        com.example.resolveit.model.User user = userRepository.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        if ("ADMIN".equals(user.getRole())) return "redirect:/admin";
        if ("STAFF".equals(user.getRole())) return "redirect:/staff-dashboard";

        java.util.Map<String, Object> stats = complaintService.getDashboardStats(user.getId());
        model.addAllAttributes(stats);

        return "dashboard";
    }


    @GetMapping("/complaint.html")
    public String complaintHtml(java.security.Principal principal, org.springframework.ui.Model model) {
        if (principal != null) {
            com.example.resolveit.model.User u = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", u);
            if (u != null) {
                // model.addAttribute("userId", u.getId()); // removed for security
            }
        }
        model.addAttribute("complaint", new com.example.resolveit.model.Complaint());
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
    public String complaintPage(java.security.Principal principal, org.springframework.ui.Model model) {
        if (principal != null) {
            com.example.resolveit.model.User u = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", u);
            if (u != null) {
                // model.addAttribute("userId", u.getId()); // removed for security
            }
        }
        model.addAttribute("complaint", new com.example.resolveit.model.Complaint());
        return "complaint";
    }

    @GetMapping("/complaint/{id}")
    public String complaintDetails(@PathVariable int id,
            Model model) {
        var complaint = complaintService.getById(id);
        if (complaint == null) {
            return "redirect:/";
        }
        var history = historyService.getByComplaintId(id);
        // Reverse so oldest first (timeline top-to-bottom)
        java.util.Collections.reverse(history);
        model.addAttribute("complaint", complaint);
        model.addAttribute("history", history);

        String backUrl = "/my-complaints";
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            com.example.resolveit.model.User currentUser = userRepository.findByEmail(auth.getName());
            if (currentUser != null) {
                if ("ADMIN".equals(currentUser.getRole())) {
                    backUrl = "/admin";
                } else if ("STAFF".equals(currentUser.getRole())) {
                    backUrl = "/staff-dashboard";
                } else {
                    backUrl = "/my-complaints";
                }
            }
        }
        model.addAttribute("backUrl", backUrl);
        return "complaint-details";
    }
}