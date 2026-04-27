package com.example.resolveit.controller;

import com.example.resolveit.model.Feedback;
import com.example.resolveit.model.User;
import com.example.resolveit.model.Complaint;
import com.example.resolveit.repository.FeedbackRepository;
import com.example.resolveit.repository.UserRepository;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.NotificationRepository;
import com.example.resolveit.service.SystemSettingService;
import com.example.resolveit.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    @Autowired private FeedbackRepository feedbackRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ComplaintRepository complaintRepo;
    @Autowired private SystemSettingService settingService;
    @Autowired private NotificationRepository notificationRepo;
    @Autowired private AnalyticsService analyticsService;

    private User getAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        User user = userRepo.findByEmail(auth.getName());
        return (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) ? user : null;
    }

    private void addCommonAttrs(Model model, User admin) {
        model.addAttribute("user", admin);
        model.addAttribute("userName", admin != null ? admin.getName() : "Admin");
        model.addAttribute("activePage", "feedback");
        model.addAttribute("pageTitle", "Feedback Management");
        model.addAttribute("appName", settingService.getSetting("APP_NAME", "ResolveIT"));
        if (admin != null) {
            model.addAttribute("unreadCount", notificationRepo.countByUserIdAndIsReadFalse(admin.getId()));
        }
    }

    @GetMapping({"", "/"})
    public String feedback(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false, defaultValue = "Latest") String sortParam,
            Model model) {
        
        User admin = getAdmin();
        if (admin == null) return "redirect:/login.html";
        
        addCommonAttrs(model, admin);

        // Sorting Logic
        Sort sort;
        switch (sortParam) {
            case "Oldest": sort = Sort.by(Sort.Direction.ASC, "createdAt"); break;
            case "Highest Rating": sort = Sort.by(Sort.Direction.DESC, "rating"); break;
            case "Lowest Rating": sort = Sort.by(Sort.Direction.ASC, "rating"); break;
            default: sort = Sort.by(Sort.Direction.DESC, "createdAt"); break;
        }

        // Date Filtering Logic
        java.time.LocalDateTime startDate = null;
        java.time.LocalDateTime endDate = java.time.LocalDateTime.now();
        if ("Today".equalsIgnoreCase(dateFilter)) {
            startDate = java.time.LocalDate.now().atStartOfDay();
        } else if ("Week".equalsIgnoreCase(dateFilter)) {
            startDate = java.time.LocalDateTime.now().minusWeeks(1);
        } else if ("Month".equalsIgnoreCase(dateFilter)) {
            startDate = java.time.LocalDateTime.now().minusMonths(1);
        }

        // Fetch data
        List<Feedback> feedbacks = feedbackRepo.findWithFilters(status, rating, staffId, keyword, startDate, endDate, sort);
        
        // Analytics
        long total = feedbackRepo.count();
        Double avgRating = feedbackRepo.getAverageRating();
        Long positive = feedbackRepo.countPositiveReviews();
        Long negative = feedbackRepo.countNegativeReviews();

        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("totalFeedback", total);
        model.addAttribute("avgRating", avgRating != null ? String.format(java.util.Locale.US, "%.1f", avgRating) : "0.0");
        model.addAttribute("positiveReviews", positive != null ? positive : 0);
        model.addAttribute("negativeReviews", negative != null ? negative : 0);

        // Filters State
        model.addAttribute("keyword", keyword);
        model.addAttribute("selRating", rating);
        model.addAttribute("selStatus", status);
        model.addAttribute("selStaffId", staffId);
        model.addAttribute("selDate", dateFilter);
        model.addAttribute("selSort", sortParam);

        // Staff List & Staff Performance Analytics
        List<User> staffList = userRepo.findAllByRole("STAFF");
        model.addAttribute("staffList", staffList);

        // Map staffId to Staff Name for easy lookup in Thymeleaf
        Map<Long, String> staffMap = staffList.stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        model.addAttribute("staffMap", staffMap);

        // Map userId to User Name (for existing records where userName might be null)
        List<Long> userIds = feedbacks.stream().map(Feedback::getUserId).distinct().collect(Collectors.toList());
        List<User> userList = userRepo.findAllById(userIds);
        Map<Long, String> userMap = userList.stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        model.addAttribute("userMap", userMap);

        // Map complaintId to Complaint object
        List<Long> complaintIds = feedbacks.stream().map(Feedback::getComplaintId).distinct().collect(Collectors.toList());
        List<Complaint> complaints = complaintRepo.findAllById(complaintIds);
        Map<Long, Complaint> complaintMap = complaints.stream()
                .collect(Collectors.toMap(Complaint::getId, c -> c));
        model.addAttribute("complaintMap", complaintMap);

        // Staff Performance Data (staffId, count, avg_rating, pos_count, neg_count)
        model.addAttribute("staffPerformance", feedbackRepo.getStaffPerformance());

        // Staff Workload (for resolved counts)
        List<Map<String, Object>> workload = analyticsService.getStaffWorkload();
        Map<Long, Long> resolvedMap = workload.stream()
                .collect(Collectors.toMap(
                    w -> {
                        // Find staff by name to get ID
                        User s = staffList.stream().filter(u -> u.getName().equals(w.get("name"))).findFirst().orElse(null);
                        return s != null ? s.getId() : -1L;
                    },
                    w -> (Long) w.get("resolved")
                ));
        model.addAttribute("resolvedMap", resolvedMap);
        
        // Rating Distribution
        model.addAttribute("ratingDistribution", feedbackRepo.getRatingDistribution());

        // Monthly Trends
        model.addAttribute("feedbackTrends", feedbackRepo.getMonthlyFeedbackTrends());

        return "admin/feedback";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Long feedbackId, @RequestParam String status, RedirectAttributes ra) {
        if (getAdmin() == null) return "redirect:/login.html";
        
        feedbackRepo.findById(feedbackId).ifPresent(f -> {
            f.setStatus(status);
            feedbackRepo.save(f);
        });
        
        ra.addFlashAttribute("success", "Feedback status updated to " + status);
        return "redirect:/admin/feedback";
    }

    @PostMapping("/delete")
    public String deleteFeedback(@RequestParam Long feedbackId, RedirectAttributes ra) {
        if (getAdmin() == null) return "redirect:/login.html";
        
        feedbackRepo.deleteById(feedbackId);
        
        ra.addFlashAttribute("success", "Feedback deleted successfully");
        return "redirect:/admin/feedback";
    }
}
