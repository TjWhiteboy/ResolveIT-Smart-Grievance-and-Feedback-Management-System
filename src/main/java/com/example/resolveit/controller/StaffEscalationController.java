package com.example.resolveit.controller;

import com.example.resolveit.model.Complaint;
import com.example.resolveit.model.Escalation;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.AttendanceRepository;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.NotificationRepository;
import com.example.resolveit.repository.UserRepository;
import com.example.resolveit.service.ComplaintService;
import com.example.resolveit.service.EscalationService;
import com.example.resolveit.service.SystemSettingService;
import com.example.resolveit.service.SlaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class StaffEscalationController {

    @Autowired private EscalationService escalationService;
    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SystemSettingService settingService;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private ComplaintService complaintService;
    @Autowired private SlaService slaService;
 
    private void addCommonAttrs(Model model, User user, String title) {
        model.addAttribute("user", user);
        model.addAttribute("userName", user.getName());
        model.addAttribute("staffId", user.getId());
        model.addAttribute("pageTitle", title);
        model.addAttribute("notifications", notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
        model.addAttribute("unreadCount", notificationRepository.countByUserIdAndIsReadFalse(user.getId()));
        
        LocalDate today = LocalDate.now();
        var todayAtt = attendanceRepository.findByUserIdAndDate(user.getId(), today);
        model.addAttribute("isCheckedIn", todayAtt != null && todayAtt.getCheckOutTime() == null);
        model.addAttribute("hasCheckedOut", todayAtt != null && todayAtt.getCheckOutTime() != null);
        model.addAttribute("todayAttendance", todayAtt);
        model.addAttribute("appName", settingService.getSetting("APP_NAME", "ResolveIT"));
    }

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        return userRepository.findByEmail(auth.getName());
    }

    @GetMapping("/staff/sla")
    public String slaMonitor(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login.html";
        addCommonAttrs(model, user, "Escalation Monitor");
        
        List<Complaint> assigned = complaintRepository.findByAssignedStaff(user.getId());
        
        List<Complaint> complaints = assigned;

        List<Complaint> overdue = complaints.stream()
            .filter(c -> slaService.getSlaStatus(c).equals("OVERDUE"))
            .collect(Collectors.toList());

        List<Complaint> dueSoon = complaints.stream()
            .filter(c -> slaService.getSlaStatus(c).equals("DUE_SOON"))
            .collect(Collectors.toList());

        List<Complaint> onTrack = complaints.stream()
            .filter(c -> slaService.getSlaStatus(c).equals("ON_TRACK"))
            .collect(Collectors.toList());

        // Sync delayed status and trigger auto-escalation check
        overdue.forEach(c -> {
            c.setDelayed(true);
            if (!escalationService.existsActiveEscalation(c.getId())) {
                escalationService.processAutoEscalation(Collections.singletonList(c), user);
            }
        });

        // Add escalation map
        Map<Long, Boolean> escalatedMap = new HashMap<>();
        complaints.forEach(c -> escalatedMap.put(c.getId(), escalationService.existsActiveEscalation(c.getId())));
        
        model.addAttribute("overdue", overdue);
        model.addAttribute("dueSoon", dueSoon);
        model.addAttribute("onTrack", onTrack);
        model.addAttribute("overdueCount", overdue.size());
        model.addAttribute("dueSoonCount", dueSoon.size());
        model.addAttribute("onTrackCount", onTrack.size());
        model.addAttribute("escalatedMap", escalatedMap);
        
        return "staff/sla";
    }

    @PostMapping("/staff/sla/raise/{id}")
    public String raiseEscalation(@PathVariable Long id,
                                  @RequestParam(required = false, defaultValue = "Manual Staff Escalation") String reason,
                                  @RequestParam(required = false, defaultValue = "MEDIUM") String severity,
                                  @RequestParam(required = false, defaultValue = "Staff requires admin intervention.") String comment,
                                  RedirectAttributes ra) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login.html";

        try {
            Complaint complaint = complaintRepository.findById(id).orElseThrow();
            
            if (escalationService.existsActiveEscalation(id)) {
                ra.addFlashAttribute("warning", "Complaint #" + id + " is already escalated.");
            } else {
                escalationService.raiseEscalation(complaint, user, reason, comment, severity);
                ra.addFlashAttribute("success", "Complaint #" + id + " raised to admin successfully.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/sla";
    }

    @PostMapping("/staff/escalation/auto-check")
    @ResponseBody
    public String autoCheckEscalations() {
        User user = getLoggedInUser();
        if (user == null) return "Unauthorized";

        List<Complaint> assigned = complaintRepository.findByAssignedStaff(user.getId());
        LocalDateTime now = LocalDateTime.now();
        List<Complaint> overdue = assigned.stream()
            .filter(complaintService::isOverdue)
            .collect(Collectors.toList());

        boolean escalated = escalationService.processAutoEscalation(overdue, user);
        return escalated ? "ESCALATED" : "OK";
    }
}
