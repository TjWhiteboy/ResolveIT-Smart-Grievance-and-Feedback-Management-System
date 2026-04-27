package com.example.resolveit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.resolveit.model.*;
import com.example.resolveit.repository.*;
import com.example.resolveit.service.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ComplaintRepository complaintRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ComplaintHistoryRepository historyRepo;
    @Autowired private NotificationRepository notificationRepo;
    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private FeedbackRepository feedbackRepo;
    @Autowired private AnalyticsService analyticsService;
    @Autowired private ReportService reportService;
    @Autowired private SystemSettingService settingService;
    @Autowired private NotificationService notificationService;
    @Autowired private com.example.resolveit.service.EmailService emailService;

    // ── Helper: Common Attributes ──
    private void addCommonAttrs(Model model, String activePage) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (auth != null) ? userRepo.findByEmail(auth.getName()) : null;
        model.addAttribute("user", user);
        model.addAttribute("userName", user != null ? user.getName() : "Admin");
        model.addAttribute("activePage", activePage);
        model.addAttribute("appName", settingService.getSetting("APP_NAME", "ResolveIT"));
        
        // Define Page Title
        String title = switch (activePage) {
            case "dashboard" -> "Admin Dashboard";
            case "complaints" -> "Complaint Management";
            case "users" -> "User Management";
            case "staff" -> "Staff Management";
            case "attendance" -> "Staff Attendance";
            case "feedback" -> "User Feedback";
            case "reports" -> "System Reports";
            case "notifications" -> "Admin Notifications";
            case "settings" -> "System Settings";
            case "profile" -> "My Profile";
            default -> "Admin Control Panel";
        };
        model.addAttribute("pageTitle", title);

        // Unread Notifications Count
        if (user != null) {
            model.addAttribute("unreadCount", notificationRepo.countByUserIdAndIsReadFalse(user.getId()));
        }
    }

    private User getAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        User user = userRepo.findByEmail(auth.getName());
        return (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) ? user : null;
    }

    // ── Dashboard ──
    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        User admin = getAdmin();
        if (admin == null) return "redirect:/login";

        addCommonAttrs(model, "dashboard");
        
        // Summary Cards
        model.addAttribute("totalComplaints", complaintRepo.count());
        model.addAttribute("newComplaints", complaintRepo.countByStatus("New"));
        model.addAttribute("reviewComplaints", complaintRepo.countByStatus("Under Review"));
        model.addAttribute("progressComplaints", complaintRepo.countByStatus("In Progress"));
        model.addAttribute("resolvedComplaints", complaintRepo.countByStatus("Resolved"));
        model.addAttribute("deniedComplaints", complaintRepo.countByStatus("Denied"));
        model.addAttribute("totalUsers", userRepo.countTotalUsers());
        model.addAttribute("totalStaff", userRepo.countByRole("STAFF"));
        model.addAttribute("presentToday", attendanceRepo.countByDateAndStatus(LocalDate.now(), "PRESENT"));
        
        // Analytics Data for Charts
        model.addAttribute("statusDistribution", analyticsService.getStatusDistribution());
        model.addAttribute("categoryDistribution", analyticsService.getCategoryDistribution());
        model.addAttribute("monthlyTrends", analyticsService.getMonthlyTrends());
        model.addAttribute("staffWorkload", analyticsService.getStaffWorkload());
        model.addAttribute("recentComplaints", complaintRepo.findTop10ByOrderByCreatedAtDesc());
        model.addAttribute("staffList", userRepo.findAllStaff());

        return "admin/dashboard";
    }

    // ── Complaint Management ──
    @GetMapping("/complaints")
    public String complaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String urgency,
            @RequestParam(required = false) String search,
            Model model) {
        if (getAdmin() == null) return "redirect:/login.html";
        addCommonAttrs(model, "complaints");
        
        List<Complaint> list = complaintRepo.findAllWithFilters(status, category, urgency, search);
        model.addAttribute("complaints", list);
        model.addAttribute("staffList", userRepo.findAllStaff());
        
        // Pass filter values back to UI
        model.addAttribute("selStatus", status);
        model.addAttribute("selCategory", category);
        model.addAttribute("selUrgency", urgency);
        model.addAttribute("keyword", search);
        
        return "admin/complaints";
    }

    @GetMapping("/details/{id}")
    public String complaintDetail(@PathVariable Long id, Model model) {
        User admin = getAdmin();
        if (admin == null) return "redirect:/login";
        addCommonAttrs(model, "complaints");

        Complaint complaint = complaintRepo.findById(id).orElse(null);
        if (complaint == null) return "redirect:/admin/complaints?error=NotFound";

        model.addAttribute("c", complaint);
        model.addAttribute("history", historyRepo.findByComplaintIdOrderByTimestampDesc(id));
        model.addAttribute("staffList", userRepo.findAllStaff());
        
        return "admin/complaint-detail";
    }

    // ── Update Complaint (unified: status + staff + notes) ──
    @PostMapping("/details/update/{id}")
    public String updateComplaintDetail(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String adminNotes) {
        if (getAdmin() == null) return "redirect:/login.html";
        complaintRepo.findById(id).ifPresent(c -> {
            String oldStatus = c.getStatus();
            c.setStatus(status);
            if (staffId != null && staffId > 0) {
                c.setAssignedStaff(staffId);
                // Notify staff of assignment
                notificationService.createNotification(staffId, id, "Assignment", "Complaint #" + id + " assigned to you.", "ASSIGNMENT", "/staff/tasks/" + id);
            }
            if (adminNotes != null && !adminNotes.isBlank()) {
                c.setAdminNotes(adminNotes);
            }
            if ("Resolved".equals(status) || "Denied".equals(status)) {
                c.setResolvedAt(LocalDateTime.now());
            }
            complaintRepo.save(c);

            // Record in history
            String noteText = "Admin updated status to " + status +
                (adminNotes != null && !adminNotes.isBlank() ? ": " + adminNotes : "");
            ComplaintHistory h = new ComplaintHistory();
            h.setComplaint(c);
            h.setStatus(status);
            h.setNote(noteText);
            h.setDescription(noteText);
            h.setUpdatedBy("Admin");
            h.setTimestamp(LocalDateTime.now());
            historyRepo.save(h);

            // Notify user
            String notifTitle = "Complaint Update";
            String notifType = "UPDATE";
            String notifMessage = "Your complaint #" + id + " has been updated.";
            
            if ("Resolved".equalsIgnoreCase(status)) {
                notifTitle = "Complaint Resolved";
                notifType = "SUCCESS";
                notifMessage = "Good news! Your complaint #" + id + " has been resolved.";
            } else if ("Denied".equalsIgnoreCase(status)) {
                notifTitle = "Complaint Denied";
                notifType = "WARNING";
                notifMessage = "Your complaint #" + id + " has been denied.";
            } else if ("In Progress".equalsIgnoreCase(status)) {
                notifTitle = "Status Update";
                notifType = "UPDATE";
                notifMessage = "Your complaint #" + id + " is now in progress.";
            }
            if (adminNotes != null && !adminNotes.isBlank()) {
                notifTitle = "Comment / Admin Response";
                notifType = "INFO";
                notifMessage = "Admin added an update for complaint #" + id + ".";
            }
            notificationService.createNotification(c.getUserId(), c.getId(), notifTitle, notifMessage, notifType);

        });
        return "redirect:/admin/details/" + id + "?success";
    }

    // ── Quick Resolve ──
    @PostMapping("/details/resolve/{id}")
    public String quickResolve(@PathVariable Long id) {
        if (getAdmin() == null) return "redirect:/login.html";
        complaintRepo.findById(id).ifPresent(c -> {
            c.setStatus("Resolved");
            c.setResolvedAt(LocalDateTime.now());
            complaintRepo.save(c);
            ComplaintHistory h = new ComplaintHistory();
            h.setComplaint(c);
            h.setStatus("Resolved");
            h.setNote("Complaint resolved by Admin.");
            h.setDescription("Complaint resolved by Admin.");
            h.setUpdatedBy("Admin");
            h.setTimestamp(LocalDateTime.now());
            historyRepo.save(h);

            notificationService.createNotification(c.getUserId(), c.getId(), "Complaint Resolved", "Good news! Your complaint #" + id + " has been resolved.", "SUCCESS");
        });
        return "redirect:/admin/complaints";
    }

    // ── Quick Reject ──
    @PostMapping("/details/reject/{id}")
    public String quickReject(@PathVariable Long id) {
        if (getAdmin() == null) return "redirect:/login.html";
        complaintRepo.findById(id).ifPresent(c -> {
            c.setStatus("Denied");
            c.setResolvedAt(LocalDateTime.now());
            complaintRepo.save(c);
            ComplaintHistory h = new ComplaintHistory();
            h.setComplaint(c);
            h.setStatus("Denied");
            h.setNote("Complaint rejected by Admin.");
            h.setDescription("Complaint rejected by Admin.");
            h.setUpdatedBy("Admin");
            h.setTimestamp(LocalDateTime.now());
            historyRepo.save(h);

            notificationService.createNotification(c.getUserId(), c.getId(), "Complaint Denied", "Your complaint #" + id + " has been denied.", "WARNING");
        });
        return "redirect:/admin/complaints";
    }

    @PostMapping("/complaints/assign")
    public String assignStaff(@RequestParam Long complaintId, @RequestParam Long staffId) {
        if (getAdmin() == null) return "redirect:/login.html";
        var complaint = complaintRepo.findById(complaintId).orElse(null);
        if (complaint != null) {
            complaint.setAssignedStaff(staffId);
            // Reset to New if it was closed/null
            if (complaint.getStatus() == null || "Resolved".equalsIgnoreCase(complaint.getStatus()) || "Denied".equalsIgnoreCase(complaint.getStatus())) {
                complaint.setStatus("New");
            }
            complaintRepo.save(complaint);
            
            notificationService.createNotification(staffId, complaintId, "Assignment", "Complaint #" + complaintId + " assigned to you.", "ASSIGNMENT", "/staff/tasks/" + complaintId);
            
            // Notify User
            User staffUser = userRepo.findById(staffId).orElse(null);
            String staffName = staffUser != null ? staffUser.getName() : "a staff member";
            notificationService.createNotification(complaint.getUserId(), complaint.getId(), "Complaint Assigned", "Your complaint #" + complaintId + " has been assigned to " + staffName + ".", "INFO");
        }
        return "redirect:/admin/complaints";
    }

    @PostMapping("/complaints/update-status")
    public String updateStatus(@RequestParam Long complaintId, @RequestParam String status) {
        if (getAdmin() == null) return "redirect:/login.html";
        complaintRepo.findById(complaintId).ifPresent(c -> {
            c.setStatus(status);
            if ("Resolved".equals(status) || "Denied".equals(status)) {
                c.setResolvedAt(LocalDateTime.now());
            }
            complaintRepo.save(c);
            
            // Add to history
            ComplaintHistory h = new ComplaintHistory();
            h.setComplaint(c);
            h.setStatus(status);
            h.setNote("Status updated by Admin via Dashboard");
            h.setDescription("Status updated by Admin via Dashboard");
            h.setUpdatedBy("Admin");
            h.setTimestamp(LocalDateTime.now());
            historyRepo.save(h);

            String notifTitle = "Complaint Update";
            String notifType = "UPDATE";
            String notifMessage = "Your complaint #" + complaintId + " has been updated.";
            
            if ("Resolved".equalsIgnoreCase(status)) {
                notifTitle = "Complaint Resolved";
                notifType = "SUCCESS";
                notifMessage = "Good news! Your complaint #" + complaintId + " has been resolved.";
            } else if ("Denied".equalsIgnoreCase(status)) {
                notifTitle = "Complaint Denied";
                notifType = "WARNING";
                notifMessage = "Your complaint #" + complaintId + " has been denied.";
            } else if ("In Progress".equalsIgnoreCase(status)) {
                notifTitle = "Status Update";
                notifType = "UPDATE";
                notifMessage = "Your complaint #" + complaintId + " is now in progress.";
            }
            notificationService.createNotification(c.getUserId(), c.getId(), notifTitle, notifMessage, notifType);

        });
        return "redirect:/admin";
    }

    // ── User Management ──
    @GetMapping("/users")
    public String users(Model model) {
        if (getAdmin() == null) return "redirect:/login.html";
        addCommonAttrs(model, "users");
        model.addAttribute("usersList", userRepo.findAllByRole("USER"));
        return "admin/users";
    }

    @PostMapping("/users/toggle-status")
    public String toggleUserStatus(@RequestParam Long userId) {
        if (getAdmin() == null) return "redirect:/login.html";
        userRepo.findById(userId).ifPresent(u -> {
            u.setEnabled(!u.isEnabled());
            userRepo.save(u);
        });
        return "redirect:/admin/users";
    }

    // ── Staff Management ──
    @GetMapping("/staff")
    public String staff(Model model) {
        if (getAdmin() == null) return "redirect:/login.html";
        addCommonAttrs(model, "staff");
        model.addAttribute("staffList", analyticsService.getStaffWorkload());
        return "admin/staff";
    }

    // ── Attendance ──
    @GetMapping("/attendance")
    public String attendance(Model model) {
        if (getAdmin() == null) return "redirect:/login.html";
        addCommonAttrs(model, "attendance");
        List<Attendance> records = attendanceRepo.findAllByOrderByDateDescCheckInTimeDesc();
        model.addAttribute("records", records);
        return "admin/attendance";
    }


    // ── Reports ──
    @GetMapping("/reports")
    public String reports(Model model) {
        if (getAdmin() == null) return "redirect:/login.html";
        addCommonAttrs(model, "reports");
        return "admin/reports";
    }

    @GetMapping("/reports/export/{type}")
    public ResponseEntity<byte[]> exportReport(@PathVariable String type) {
        if (getAdmin() == null) return ResponseEntity.status(403).build();
        String content = "";
        String filename = "report_" + type + ".csv";
        
        switch (type) {
            case "complaints": content = reportService.generateComplaintReport(); break;
            case "users": content = reportService.generateUserReport(); break;
            case "attendance": content = reportService.generateAttendanceReport(); break;
            case "staff": content = reportService.generateStaffReport(); break;
            case "feedback": content = reportService.generateFeedbackReport(); break;
            default: content = "No data";
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content.getBytes());
    }

    // ── Settings ──
    @GetMapping("/settings")
    public String settings(Model model) {
        if (getAdmin() == null) return "redirect:/login.html";
        addCommonAttrs(model, "settings");
        model.addAttribute("settings", settingService.getAllSettings());
        return "admin/settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam Map<String, String> params) {
        if (getAdmin() == null) return "redirect:/login.html";
        params.forEach((k, v) -> {
            if (!k.equals("_csrf")) settingService.updateSetting(k, v);
        });
        return "redirect:/admin/settings?success";
    }

    // ── Notifications ──
    @GetMapping("/notifications")
    public String notifications(Model model) {
        User admin = getAdmin();
        if (admin == null) return "redirect:/login";
        addCommonAttrs(model, "notifications");
        
        // Fetch both user-specific and role-based admin notifications
        List<Notification> personal = notificationRepo.findByUserIdOrderByCreatedAtDesc(admin.getId());
        List<Notification> roleBased = notificationRepo.findByRoleOrderByCreatedAtDesc("ADMIN");
        
        List<Notification> all = new ArrayList<>(personal);
        all.addAll(roleBased);
        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        model.addAttribute("notifications", all);
        model.addAttribute("unreadCount", notificationRepo.countByUserIdAndIsReadFalse(admin.getId()) + 
                                        notificationRepo.countByRoleAndIsReadFalse("ADMIN"));
        return "admin/notifications";
    }

    // ── Profile ──
    @GetMapping("/profile")
    public String profile(Model model) {
        User admin = getAdmin();
        if (admin == null) return "redirect:/login";
        addCommonAttrs(model, "profile");
        
        // Match ProfileController logic for the modern theme
        int completion = 0;
        if (admin.getName() != null && !admin.getName().isEmpty()) completion += 20;
        if (admin.getEmail() != null && !admin.getEmail().isEmpty()) completion += 20;
        if (admin.getPhone() != null && !admin.getPhone().isEmpty()) completion += 15;
        if (admin.getAddress() != null && !admin.getAddress().isEmpty()) completion += 15;
        if (admin.getBio() != null && !admin.getBio().isEmpty()) completion += 15;
        if (admin.getProfilePicturePath() != null) completion += 15;
        
        model.addAttribute("completion", Math.min(completion, 100));
        model.addAttribute("pageTitle", "Admin Control Panel");
        model.addAttribute("userRole", "ADMIN");
        
        return "common/profile";

    }
}
