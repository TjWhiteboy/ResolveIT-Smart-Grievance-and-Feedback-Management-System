package com.example.resolveit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.resolveit.model.*;
import com.example.resolveit.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ComplaintHistoryRepository complaintHistoryRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private com.example.resolveit.service.EmailService emailService;
    @Autowired private com.example.resolveit.service.NotificationService notificationService;
    @Autowired private com.example.resolveit.service.ComplaintService complaintService;
    @Autowired private com.example.resolveit.service.EscalationService escalationService;
    @Autowired private com.example.resolveit.service.SlaService slaService;

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userRepository.findByEmail(auth.getName());
        }
        return null;
    }

    private void addCommonAttrs(Model model, User user, String title) {
        model.addAttribute("user", user);
        model.addAttribute("userName", user.getName());
        model.addAttribute("staffId", user.getId());
        model.addAttribute("pageTitle", title);
        model.addAttribute("notifications", notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
        model.addAttribute("unreadCount", notificationRepository.countByUserIdAndIsReadFalse(user.getId()));
        
        LocalDate today = LocalDate.now();
        Attendance todayAtt = attendanceRepository.findByUserIdAndDate(user.getId(), today);
        model.addAttribute("isCheckedIn", todayAtt != null && todayAtt.getCheckOutTime() == null);
        model.addAttribute("hasCheckedOut", todayAtt != null && todayAtt.getCheckOutTime() != null);
        model.addAttribute("todayAttendance", todayAtt);
    }

    // ── 1. Dashboard ──
    @GetMapping("/dashboard")
    public String staffDashboard(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login.html";
        if ("ADMIN".equals(user.getRole())) return "redirect:/admin";
        if ("USER".equals(user.getRole())) return "redirect:/my-complaints";

        addCommonAttrs(model, user, "Staff Dashboard");

        List<Complaint> assigned = complaintRepository.findByAssignedStaff(user.getId());

        // SLA Monitor - Notify and mark as delayed
        assigned.forEach(c -> {
            if (!complaintService.isResolved(c)) {
                if (complaintService.isOverdue(c)) {
                    if (!c.isDelayed()) {
                        c.setDelayed(true);
                        complaintRepository.save(c);
                        
                        notificationService.createNotification(
                            user.getId(),
                            c.getId(),
                            "SLA Alert",
                            "Complaint #" + c.getId() + " has exceeded the 48-hour SLA!",
                            "SLA_ALERT"
                        );
                    }
                }
            }
        });

        // ── Stats for Cards ──
        LocalDate today = LocalDate.now();
        List<Complaint> activeTasks = assigned.stream()
            .filter(c -> c.getStatus() == null || (!"Resolved".equalsIgnoreCase(c.getStatus()) && !"Completed".equalsIgnoreCase(c.getStatus()) && !"Closed".equalsIgnoreCase(c.getStatus())))
            .collect(Collectors.toList());

        model.addAttribute("totalAssigned", (long) activeTasks.size());
        model.addAttribute("pendingCount", activeTasks.stream().filter(c -> "New".equalsIgnoreCase(c.getStatus()) || "Under Review".equalsIgnoreCase(c.getStatus())).count());
        model.addAttribute("inProgressCount", activeTasks.stream().filter(c -> "In Progress".equalsIgnoreCase(c.getStatus())).count());
        model.addAttribute("resolvedToday", assigned.stream()
            .filter(c -> "Resolved".equalsIgnoreCase(c.getStatus()) && c.getResolvedAt() != null && c.getResolvedAt().toLocalDate().equals(today))
            .count());
        model.addAttribute("delayedCount", activeTasks.stream().filter(Complaint::isDelayed).count());
        model.addAttribute("totalCompleted", assigned.stream()
            .filter(c -> c.getStatus() != null && ("Resolved".equalsIgnoreCase(c.getStatus()) || "Completed".equalsIgnoreCase(c.getStatus()) || "Closed".equalsIgnoreCase(c.getStatus())))
            .count());

        // ── Data for Charts ──
        // 1. Status Distribution
        Map<String, Long> statusCounts = assigned.stream()
            .collect(Collectors.groupingBy(Complaint::getStatus, Collectors.counting()));
        model.addAttribute("statusChartData", statusCounts);

        // 2. Category Distribution
        Map<String, Long> catCounts = assigned.stream()
            .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));
        model.addAttribute("categoryChartData", catCounts);

        // 3. Weekly Productivity (Resolved per day last 7 days)
        Map<String, Long> productivity = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long count = assigned.stream()
                .filter(c -> "Resolved".equals(c.getStatus()) && c.getResolvedAt() != null && c.getResolvedAt().toLocalDate().equals(date))
                .count();
            productivity.put(date.getDayOfWeek().toString().substring(0, 3), count);
        }
        model.addAttribute("productivityChartData", productivity);

        return "staff/dashboard";
    }


    // ── 2. My Tasks ──
    @GetMapping("/tasks")
    public String myTasks(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        addCommonAttrs(model, user, "My Tasks");
        List<Complaint> assigned = complaintRepository.findByAssignedStaff(user.getId());
        List<Complaint> tasks = assigned.stream()
            .filter(c -> c.getStatus() == null || (!"Resolved".equalsIgnoreCase(c.getStatus()) && !"Completed".equalsIgnoreCase(c.getStatus()) && !"Closed".equalsIgnoreCase(c.getStatus())))
            .collect(Collectors.toList());
            
        // Dynamically update 'delayed' status before rendering
        tasks.forEach(c -> c.setDelayed(complaintService.isOverdue(c)));
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("totalTasks", tasks.size());
        return "staff/tasks";
    }

    @GetMapping("/completed")
    public String completedComplaints(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        addCommonAttrs(model, user, "Completed Complaints");
        
        List<Complaint> assigned = complaintRepository.findByAssignedStaff(user.getId());
        List<Complaint> completed = assigned.stream()
            .filter(c -> c.getStatus() != null && ("Resolved".equalsIgnoreCase(c.getStatus()) || "Completed".equalsIgnoreCase(c.getStatus()) || "Closed".equalsIgnoreCase(c.getStatus())))
            .collect(Collectors.toList());

        long totalCompleted = completed.size();
        
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1); // Exact start of current week (Monday)

        long completedToday = completed.stream()
            .filter(c -> c.getResolvedAt() != null && c.getResolvedAt().toLocalDate().equals(today))
            .count();
            
        long completedThisWeek = completed.stream()
            .filter(c -> c.getResolvedAt() != null && !c.getResolvedAt().toLocalDate().isBefore(startOfWeek))
            .count();

        double avgResolutionDays = 0.0;
        long totalDays = 0;
        int validCount = 0;
        for (Complaint c : completed) {
            if (c.getCreatedAt() != null && c.getResolvedAt() != null && !c.getCreatedAt().isAfter(c.getResolvedAt())) {
                totalDays += Duration.between(c.getCreatedAt(), c.getResolvedAt()).toDays();
                validCount++;
            }
        }
        if (validCount > 0) {
            avgResolutionDays = (double) totalDays / validCount;
        }

        model.addAttribute("completedTasks", completed);
        model.addAttribute("totalCompleted", totalCompleted);
        model.addAttribute("completedToday", completedToday);
        model.addAttribute("completedThisWeek", completedThisWeek);
        model.addAttribute("avgResolutionDays", String.format("%.1f", avgResolutionDays)); // String format for UI
        
        return "staff/completed";
    }

    // ── 3. All Complaints ──
    @GetMapping("/complaints")
    public String allComplaints(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        addCommonAttrs(model, user, "All Complaints");
        List<Complaint> all = complaintRepository.findAll();
        // Dynamically update 'delayed' status before rendering
        all.forEach(c -> c.setDelayed(complaintService.isOverdue(c)));
        
        model.addAttribute("complaints", all);
        model.addAttribute("totalComplaints", all.size());
        return "staff/complaints";
    }

    // ── 4. Notifications ──
    @GetMapping("/notifications")
    public String staffNotifications(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        addCommonAttrs(model, user, "Notifications");
        
        // Fetch both user-specific and role-based notifications
        List<Notification> personal = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<Notification> roleBased = notificationRepository.findByRoleOrderByCreatedAtDesc("STAFF");
        
        List<Notification> all = new ArrayList<>(personal);
        all.addAll(roleBased);
        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        model.addAttribute("notifications", all);
        model.addAttribute("unreadCount", notificationRepository.countByUserIdAndIsReadFalse(user.getId()) + 
                                        notificationRepository.countByRoleAndIsReadFalse("STAFF"));
        
        return "staff/notifications";
    }

    // ── 5. Timeline ──
    @GetMapping("/timeline")
    public String timeline(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        addCommonAttrs(model, user, "Timeline");
        List<Complaint> assigned = complaintRepository.findByAssignedStaff(user.getId());
        List<ComplaintHistory> allHistory = new ArrayList<>();
        for (Complaint c : assigned) {
            allHistory.addAll(complaintHistoryRepository.findByComplaintIdOrderByTimestampDesc(c.getId()));
        }
        allHistory.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        model.addAttribute("timeline", allHistory);
        return "staff/timeline";
    }

    // ── 7. Feedback ──
    @GetMapping("/feedback")
    public String feedback(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        addCommonAttrs(model, user, "Feedback");
        List<Complaint> assigned = complaintRepository.findByAssignedStaff(user.getId());
        List<Map<String, Object>> feedbackList = new ArrayList<>();
        double totalRating = 0;
        int count = 0;
        for (Complaint c : assigned) {
            Feedback fb = feedbackRepository.findByComplaintId(c.getId());
            if (fb != null) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("feedback", fb);
                entry.put("complaint", c);
                User u = userRepository.findById(c.getUserId()).orElse(null);
                entry.put("userName", u != null ? u.getName() : "Unknown");
                feedbackList.add(entry);
                totalRating += fb.getRating();
                count++;
            }
        }
        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("feedbackCount", count);
        model.addAttribute("avgRating", count > 0 ? String.format("%.1f", totalRating / count) : "N/A");
        return "staff/feedback";
    }

    @GetMapping("/profile")
    public String redirectOldStaffProfile() {
        return "redirect:/profile";
    }

    // ── 9. Activity Log ──
    @GetMapping("/activity-log")
    public String activityLog(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        addCommonAttrs(model, user, "Activity Log");
        List<Complaint> assigned = complaintRepository.findByAssignedStaff(user.getId());
        List<Map<String, Object>> activities = new ArrayList<>();

        for (Complaint c : assigned) {
            for (ComplaintHistory h : complaintHistoryRepository.findByComplaintIdOrderByTimestampDesc(c.getId())) {
                Map<String, Object> a = new HashMap<>();
                a.put("time", h.getTimestamp());
                a.put("description", h.getDescription());
                a.put("type", "STATUS");
                a.put("icon", "fa-solid fa-arrows-rotate");
                activities.add(a);
            }
        }
        List<Attendance> attendances = attendanceRepository.findByUserId(user.getId());
        for (Attendance att : attendances) {
            Map<String, Object> a = new HashMap<>();
            a.put("time", att.getCheckInTime());
            a.put("description", "Checked in for duty");
            a.put("type", "CHECKIN");
            a.put("icon", "fa-solid fa-right-to-bracket");
            activities.add(a);
            if (att.getCheckOutTime() != null) {
                Map<String, Object> b = new HashMap<>();
                b.put("time", att.getCheckOutTime());
                b.put("description", "Checked out");
                b.put("type", "CHECKOUT");
                b.put("icon", "fa-solid fa-right-from-bracket");
                activities.add(b);
            }
        }
        activities.sort((a, b) -> ((LocalDateTime) b.get("time")).compareTo((LocalDateTime) a.get("time")));
        model.addAttribute("activities", activities);
        return "staff/activity-log";
    }

    // ── 10. Specialized Detail Views ──
    @GetMapping("/complaint/view/{id}")
    public String viewComplaint(@PathVariable Long id, 
                                @RequestParam(required = false) String from,
                                Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        
        Complaint complaint = complaintService.getById(id);
        addCommonAttrs(model, user, "Complaint Details");
        
        // Enrichment
        User creator = userRepository.findById(complaint.getUserId()).orElse(null);
        model.addAttribute("creator", creator);
        
        if (complaint.getAssignedStaff() != null) {
            User staff = userRepository.findById(complaint.getAssignedStaff()).orElse(null);
            model.addAttribute("assignedStaffUser", staff);
        }

        // SLA Logic
        String slaStatus = slaService.getSlaStatus(complaint);
        model.addAttribute("slaStatus", slaStatus);
        model.addAttribute("isOverdue", "OVERDUE".equals(slaStatus));
        model.addAttribute("isDueSoon", "DUE_SOON".equals(slaStatus));
        model.addAttribute("isOnTime", "ON_TRACK".equals(slaStatus));
        
        long elapsedHours = slaService.getElapsedHours(complaint);
        model.addAttribute("elapsedHours", elapsedHours);
        
        if (complaint.getCreatedAt() != null) {
            LocalDateTime end = (complaint.getStatus() != null && (complaint.getStatus().equalsIgnoreCase("Resolved") || complaint.getStatus().equalsIgnoreCase("Denied") || complaint.getStatus().equalsIgnoreCase("Closed"))) 
                                ? complaint.getUpdatedAt() : LocalDateTime.now();
            if (end == null) end = LocalDateTime.now();
            model.addAttribute("daysOpen", Duration.between(complaint.getCreatedAt(), end).toDays());
        }
        
        var history = complaintHistoryRepository.findByComplaintIdOrderByTimestampDesc(id);
        Collections.reverse(history);
        
        model.addAttribute("complaint", complaint);
        model.addAttribute("from", from);
        model.addAttribute("history", history);
        model.addAttribute("isEscalated", escalationService.existsActiveEscalation(id));
        model.addAttribute("activeSidebar", "sla".equals(from) ? "sla" : "complaints");

        return "staff/complaint-view";
    }

    @GetMapping("/tasks/{id}")
    public String staffTaskDetails(@PathVariable Long id, Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        return showComplaintDetail(id, user, model, false, "/staff/tasks", "tasks");
    }

    @GetMapping("/complaints/{id}")
    public String staffComplaintDetails(@PathVariable Long id, Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        return showComplaintDetail(id, user, model, true, "/staff/complaints", "complaints");
    }

    private String showComplaintDetail(Long id, User user, Model model, boolean readOnly, String backUrl, String activePage) {
        Complaint complaint = complaintRepository.findById(id).orElse(null);
        if (complaint == null) return "redirect:/staff/dashboard";

        addCommonAttrs(model, user, "Complaint Details");
        
        var history = complaintHistoryRepository.findByComplaintIdOrderByTimestampDesc(id);
        Collections.reverse(history);
        
        // Enrichment
        User creator = userRepository.findById(complaint.getUserId()).orElse(null);
        model.addAttribute("creator", creator);
        
        if (complaint.getAssignedStaff() != null) {
            User staff = userRepository.findById(complaint.getAssignedStaff()).orElse(null);
            model.addAttribute("assignedStaffUser", staff);
        }

        // SLA Logic
        String slaCategory = complaintService.getSLACategory(complaint);
        model.addAttribute("isOverdue", "OVERDUE".equals(slaCategory));
        model.addAttribute("isDueSoon", "DUE_SOON".equals(slaCategory));
        model.addAttribute("isOnTime", "ON_TRACK".equals(slaCategory));

        if (complaint.getCreatedAt() != null) {
            LocalDateTime end = complaint.getResolvedAt() != null ? complaint.getResolvedAt() : LocalDateTime.now();
            model.addAttribute("daysOpen", Duration.between(complaint.getCreatedAt(), end).toDays());
        }
        
        model.addAttribute("complaint", complaint);
        model.addAttribute("history", history);
        model.addAttribute("readOnly", readOnly);
        model.addAttribute("backUrl", backUrl);
        model.addAttribute("activeSidebar", activePage);

        Feedback fb = feedbackRepository.findByComplaintId(id);
        model.addAttribute("feedback", fb);
        model.addAttribute("hasFeedback", fb != null);
        
        return "staff/complaint-detail";
    }

    // ── Status Update ──
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam Long id,
            @RequestParam String status,
            @RequestParam(required = false) Long staffId,
            RedirectAttributes ra) {
        var complaint = complaintRepository.findById(id).orElse(null);
        if (complaint != null) {
            String oldStatus = complaint.getStatus();
            complaint.setStatus(status);
            if ("Resolved".equals(status)) {
                complaint.setResolvedAt(LocalDateTime.now());
                complaint.setDelayed(false);
            }
            complaintRepository.save(complaint);

            String updatedBy = "Staff";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) updatedBy = auth.getName();

            ComplaintHistory history = new ComplaintHistory();
            history.setComplaint(complaint);
            history.setStatus(status);
            history.setNote("Status changed from " + oldStatus + " to " + status);
            history.setDescription("Staff updated status to " + status);
            history.setUpdatedBy(updatedBy);
            history.setTimestamp(LocalDateTime.now());
            complaintHistoryRepository.save(history);

            if (staffId != null) {
                notificationService.createNotification(staffId, id, "Status Update", "Complaint #" + id + " status updated to " + status, "STATUS_UPDATE", "/staff/tasks/" + id);
            }

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
            } else if ("Under Review".equalsIgnoreCase(status)) {
                notifTitle = "Status Update";
                notifType = "INFO";
                notifMessage = "Complaint #" + id + " is now under review.";
            }
            notificationService.createNotification(complaint.getUserId(), complaint.getId(), notifTitle, notifMessage, notifType);

            User user = userRepository.findById(complaint.getUserId()).orElse(null);
            if (user != null && user.getEmail() != null) {
                emailService.sendEmail(user.getEmail(),
                    "Complaint Status Updated - ResolveIT",
                    "Hello " + user.getName() + ",\n\nThe status of your complaint #" + complaint.getId() + " ('" + complaint.getTitle() + "') has been updated.\n\nNew Status: " + status + "\n\nThank you for using ResolveIT.");
            }
            ra.addFlashAttribute("success", "Status updated successfully!");
        }
        return "redirect:/staff/dashboard";

    }
}
