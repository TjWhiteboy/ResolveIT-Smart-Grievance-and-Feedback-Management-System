package com.example.resolveit.controller;

import com.example.resolveit.model.Escalation;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.UserRepository;
import com.example.resolveit.repository.NotificationRepository;
import com.example.resolveit.repository.ComplaintHistoryRepository;
import com.example.resolveit.service.NotificationService;
import com.example.resolveit.model.ComplaintHistory;
import com.example.resolveit.model.Notification;
import com.example.resolveit.service.EscalationService;
import com.example.resolveit.service.SystemSettingService;
import com.example.resolveit.service.SlaService;
import com.example.resolveit.model.Complaint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminEscalationController {

    @Autowired private EscalationService escalationService;
    @Autowired private UserRepository userRepository;
    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private SystemSettingService settingService;
    @Autowired private NotificationRepository notificationRepo;
    @Autowired private ComplaintHistoryRepository historyRepo;
    @Autowired private NotificationService notificationService;
    @Autowired private SlaService slaService;

    private void addCommonAttrs(Model model, String pageTitle, String activePage) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (auth != null) ? userRepository.findByEmail(auth.getName()) : null;
        model.addAttribute("user", user);
        model.addAttribute("userName", user != null ? user.getName() : "Admin");
        model.addAttribute("activePage", activePage);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("appName", settingService.getSetting("APP_NAME", "ResolveIT"));
        
        if (user != null) {
            model.addAttribute("unreadCount", notificationRepo.countByUserIdAndIsReadFalse(user.getId()));
        }
    }

    private User getAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        User user = userRepository.findByEmail(auth.getName());
        return (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) ? user : null;
    }

    @GetMapping("/sla")
    public String adminSla(Model model) {
        User admin = getAdmin();
        if (admin == null) return "redirect:/login.html";

        addCommonAttrs(model, "Escalation Monitor", "sla");
        
        List<Complaint> complaints = complaintRepository.findAll();

        List<Complaint> escalations = new ArrayList<>();
        int overdueCount = 0;
        int dueSoonCount = 0;
        int resolvedCount = 0;
        int criticalCount = 0;

        for (Complaint c : complaints) {
            String status = slaService.getSlaStatus(c);
            if ("Resolved".equalsIgnoreCase(c.getStatus()) || "Denied".equalsIgnoreCase(c.getStatus())) {
                resolvedCount++;
            } else {
                escalations.add(c); // Only show active complaints in the table
                if ("OVERDUE".equals(status)) {
                    overdueCount++;
                    if ("High".equalsIgnoreCase(c.getUrgency())) {
                        criticalCount++;
                    }
                } else if ("DUE_SOON".equals(status)) {
                    dueSoonCount++;
                }
            }
        }

        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("dueSoonCount", dueSoonCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("criticalCount", criticalCount);
        model.addAttribute("escalations", escalations);
        
        return "admin/escalation-monitor";
    }

    @GetMapping("/sla/view/{id}")
    public String viewEscalationComplaint(@PathVariable Long id, Model model){
        User admin = getAdmin();
        if (admin == null) return "redirect:/login.html";
        
        addCommonAttrs(model, "Escalation Complaint Details", "sla");
        Complaint complaint = complaintRepository.findById(id).orElse(null);
        if (complaint == null) return "redirect:/admin/sla";
        
        model.addAttribute("complaint", complaint);
        model.addAttribute("c", complaint); // Add as 'c' as well, so we can reuse complaint-detail view easily if needed
        model.addAttribute("history", historyRepo.findByComplaintIdOrderByTimestampDesc(id));
        model.addAttribute("staffList", userRepository.findAllByRole("STAFF"));
        return "admin/escalation-view";
    }

    @PostMapping("/sla/view/update/{id}")
    public String updateComplaintDetail(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String adminNotes,
            RedirectAttributes ra) {
        if (getAdmin() == null) return "redirect:/login.html";
        complaintRepository.findById(id).ifPresent(c -> {
            c.setStatus(status);
            if (staffId != null && staffId > 0) {
                c.setAssignedStaff(staffId);
                notificationService.createNotification(staffId, id, "Assignment", "Escalated Complaint #" + id + " assigned to you.", "ASSIGNMENT", "/staff/tasks/" + id);
            }
            if (adminNotes != null && !adminNotes.isBlank()) {
                c.setAdminNotes(adminNotes);
            }
            if ("Resolved".equals(status) || "Denied".equals(status)) {
                c.setResolvedAt(LocalDateTime.now());
            }
            complaintRepository.save(c);

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

            String notifTitle = "Complaint Update";
            String notifMessage = "Your escalated complaint #" + id + " has been updated.";
            String notifType = "UPDATE";
            if ("Resolved".equalsIgnoreCase(status)) {
                notifTitle = "Complaint Resolved";
                notifMessage = "Good news! Your complaint #" + id + " has been resolved.";
                notifType = "SUCCESS";
            } else if ("Denied".equalsIgnoreCase(status)) {
                notifTitle = "Complaint Denied";
                notifMessage = "Your complaint #" + id + " has been denied.";
                notifType = "WARNING";
            }
            notificationService.createNotification(c.getUserId(), c.getId(), notifTitle, notifMessage, notifType);
        });
        ra.addFlashAttribute("success", "Update successful!");
        return "redirect:/admin/sla/view/" + id;
    }

    @PostMapping("/sla/view/resolve/{id}")
    public String quickResolve(@PathVariable Long id, RedirectAttributes ra) {
        if (getAdmin() == null) return "redirect:/login.html";
        complaintRepository.findById(id).ifPresent(c -> {
            c.setStatus("Resolved");
            c.setResolvedAt(LocalDateTime.now());
            complaintRepository.save(c);
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
        ra.addFlashAttribute("success", "Complaint resolved successfully!");
        return "redirect:/admin/sla/view/" + id;
    }

    @PostMapping("/sla/view/reject/{id}")
    public String quickReject(@PathVariable Long id, RedirectAttributes ra) {
        if (getAdmin() == null) return "redirect:/login.html";
        complaintRepository.findById(id).ifPresent(c -> {
            c.setStatus("Denied");
            c.setResolvedAt(LocalDateTime.now());
            complaintRepository.save(c);
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
        ra.addFlashAttribute("success", "Complaint rejected!");
        return "redirect:/admin/sla/view/" + id;
    }
}
