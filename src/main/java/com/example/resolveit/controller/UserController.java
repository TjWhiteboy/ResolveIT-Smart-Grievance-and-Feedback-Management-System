package com.example.resolveit.controller;

import java.time.LocalDateTime;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CookieValue;

import com.example.resolveit.model.Complaint;
import com.example.resolveit.model.Feedback;
import com.example.resolveit.model.Notification;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.FeedbackRepository;
import com.example.resolveit.repository.NotificationRepository;
import com.example.resolveit.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UserController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private static final int PAGE_SIZE = 10;

    @GetMapping("/staff-dashboard")
    public String legacyStaffDashboardRedirect() {
        return "redirect:/staff/dashboard";
    }

    // ── My Complaints (with search, filter, pagination) ──────────────────
    @GetMapping("/my-complaints")
    public String myComplaints(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "0") int page,
            @CookieValue(name = "jwtToken", required = false) String token,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/login.html";
        }
        User currentUser = userRepository.findByEmail(auth.getName());
        if (currentUser != null) {
            if ("ADMIN".equals(currentUser.getRole())) return "redirect:/admin";
            if ("STAFF".equals(currentUser.getRole())) return "redirect:/staff/dashboard";
            if (userId == null || userId <= 0L) {
                userId = currentUser.getId();
            }
            model.addAttribute("user", currentUser);
            model.addAttribute("userName", currentUser.getName());
            model.addAttribute("unreadCount", notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId()));
        }

        // ── Parse dates ──
        java.time.LocalDateTime start = null;
        java.time.LocalDateTime end = null;
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            if (startDate != null && !startDate.isEmpty()) {
                start = java.time.LocalDate.parse(startDate, dateFmt).atStartOfDay();
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = java.time.LocalDate.parse(endDate, dateFmt).atTime(java.time.LocalTime.MAX);
            }
        } catch (Exception e) {
            // ignore parse errors
        }

        // ── Fetch & filter complaints using advanced repository method ──
        List<Complaint> allComplaints = complaintRepository.findByUserIdAndFilters(userId, status, search, start, end);

        // ── Stats (always from all user complaints, not filtered) ──
        List<Complaint> userComplaints = complaintRepository.findByUserId(userId);
        long totalCount = userComplaints.size();
        long newCount = userComplaints.stream().filter(c -> "New".equals(c.getStatus())).count();
        long reviewCount = userComplaints.stream().filter(c -> "Under Review".equals(c.getStatus())).count();
        long resolvedCount = userComplaints.stream().filter(c -> "Resolved".equals(c.getStatus())).count();
        long inProgressCount = userComplaints.stream().filter(c -> "In Progress".equals(c.getStatus())).count();
        long deniedCount = userComplaints.stream().filter(c -> "Denied".equals(c.getStatus())).count();

        // ── Recent Activity (last 5) ──
        List<Complaint> recentComplaints = userComplaints.stream()
                .sorted((c1, c2) -> {
                    LocalDateTime t1 = c1.getUpdatedAt() != null ? c1.getUpdatedAt() : c1.getCreatedAt();
                    LocalDateTime t2 = c2.getUpdatedAt() != null ? c2.getUpdatedAt() : c2.getCreatedAt();
                    return t2.compareTo(t1);
                })
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        // ── Pagination ──
        int totalItems = allComplaints.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        int fromIndex = page * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalItems);
        List<Complaint> pagedComplaints = allComplaints.subList(
                Math.min(fromIndex, totalItems),
                Math.min(toIndex, totalItems));

        // ── Model attributes ──
        model.addAttribute("complaints", pagedComplaints);
        model.addAttribute("userId", userId);
        model.addAttribute("token", token);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);

        // Stats
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("newCount", newCount);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("deniedCount", deniedCount);
        model.addAttribute("recentComplaints", recentComplaints);

        return "my-complaints";
    }

    // ── CSV Export ────────────────────────────────────────────────────────
    @GetMapping({"/export", "/my-complaints/export"})
    public void exportCsv(
            @RequestParam(required = false) Long userId,
            @CookieValue(name = "jwtToken", required = false) String token,
            HttpServletResponse response) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            response.sendRedirect("/login.html");
            return;
        }
        User currentUser = userRepository.findByEmail(auth.getName());
        if (currentUser != null && (userId == null || userId <= 0L)) {
            userId = currentUser.getId();
        }

        List<Complaint> complaints = complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"my-complaints.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("id,title,status,created_at");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Complaint c : complaints) {
            String createdAt = c.getCreatedAt() != null ? c.getCreatedAt().format(fmt) : "";
            String title = escapeCsv(c.getTitle());
            writer.println(c.getId() + "," + title + "," + c.getStatus() + "," + createdAt);
        }
        writer.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ── Feedback Submission ──────────────────────────────────────────────
    @PostMapping("/feedback")
    public String submitFeedback(
            @RequestParam Long complaintId,
            @RequestParam int rating,
            @RequestParam(required = false, defaultValue = "") String comment) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return "redirect:/login.html";
        }
        User currentUser = userRepository.findByEmail(auth.getName());
        if (currentUser == null) {
            return "redirect:/login.html";
        }

        // Validate complaint exists and is resolved
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        if (complaint == null || !"Resolved".equals(complaint.getStatus())) {
            return "redirect:/my-complaints?error=Feedback is only allowed for resolved complaints";
        }

        // Prevent duplicate feedback
        if (feedbackRepository.existsByComplaintId(complaintId)) {
            return "redirect:/complaint/" + complaintId + "?userId=" + currentUser.getId() + "&info=Feedback already submitted";
        }

        // Validate rating range
        if (rating < 1) rating = 1;
        if (rating > 5) rating = 5;

        Feedback feedback = new Feedback();
        feedback.setComplaintId(complaintId);
        feedback.setUserId(currentUser.getId());
        feedback.setUserName(currentUser.getName());
        feedback.setStaffId(complaint.getAssignedStaff());
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setStatus("Pending Review");
        feedbackRepository.save(feedback);

        return "redirect:/complaint/" + complaintId + "?userId=" + currentUser.getId();
    }

    @GetMapping("/user/notifications")
    public String notificationsPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login.html";
        }
        
        User currentUser = userRepository.findByEmail(auth.getName());
        if (currentUser == null) return "redirect:/login.html";

        // Redirect staff/admin if they hit the general notifications link
        if ("ADMIN".equals(currentUser.getRole())) return "redirect:/admin/notifications";
        if ("STAFF".equals(currentUser.getRole())) return "redirect:/staff/notifications";

        // Fetch both user-specific and role-based notifications
        List<Notification> personal = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        List<Notification> roleBased = notificationRepository.findByRoleOrderByCreatedAtDesc("USER");
        
        List<Notification> all = new ArrayList<>(personal);
        all.addAll(roleBased);
        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        model.addAttribute("user", currentUser);
        model.addAttribute("userId", currentUser.getId());
        model.addAttribute("userName", currentUser.getName());
        model.addAttribute("notifications", all);
        model.addAttribute("unreadCount", notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId()) +
                                        notificationRepository.countByRoleAndIsReadFalse("USER"));
        model.addAttribute("activePage", "notifications");
        
        return "user/notifications";
    }
}
