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

@Controller
public class StaffController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.resolveit.repository.ComplaintRepository complaintRepository;

    @GetMapping("/staff-dashboard")
    public String staffDashboard(java.security.Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        com.example.resolveit.model.User user = userRepository.findByEmail(principal.getName());
        if (user == null || !"STAFF".equals(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("stats", complaintService.getStaffDashboardStats(user.getId()));
        model.addAttribute("recentComplaints", complaintService.findByAssignedStaff(user.getId()).stream().limit(5).toList());
        model.addAttribute("userName", user.getName());
        model.addAttribute("pageTitle", "Staff Workspace Overview");

        return "staff-dashboard";
    }

    @PostMapping("/staff/updateStatus")
    public String updateStatus(@RequestParam int id,
            @RequestParam String status,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) Integer staffId) {
        
        String updatedBy = "Staff";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            updatedBy = auth.getName();
        }

        complaintService.updateStatus(id, status, note != null ? note : "Status updated to " + status, updatedBy);

        String redirect = "/staff-dashboard";
        if (staffId != null) {
            redirect += "?staffId=" + staffId;
        }
        return "redirect:" + redirect;
    }
    @PostMapping("/staff/update-status")
    public String updateStatusDropdown(
            @RequestParam int complaintId,
            @RequestParam String status,
            @RequestParam(required = false) String returnStatus) {
        
        String updatedBy = "Staff";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            updatedBy = auth.getName();
        }

        complaintService.updateStatus(complaintId, status, "Status updated to " + status + " via dropdown", updatedBy);

        String redirect = "/staff/assigned?success=" + status;
        if (returnStatus != null && !returnStatus.isEmpty() && !returnStatus.equals("ALL")) {
            redirect += "&status=" + returnStatus;
        }
        return "redirect:" + redirect;
    }

    @PostMapping("/staff/quick-action")
    public String quickAction(
            @RequestParam int complaintId,
            @RequestParam String status,
            @RequestParam(required = false) String returnStatus) {
        
        String updatedBy = "Staff";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            updatedBy = auth.getName();
        }

        complaintService.updateStatus(complaintId, status, "Quick action: Status set to " + status, updatedBy);

        String redirect = "/staff/assigned?success=" + status;
        if (returnStatus != null && !returnStatus.isEmpty() && !returnStatus.equals("ALL")) {
            redirect += "&status=" + returnStatus;
        }
        return "redirect:" + redirect;
    }

    @GetMapping("/staff/assigned")
    public String myTasks(java.security.Principal principal, Model model,
                         @RequestParam(defaultValue = "0") int page) {
        if (principal == null) return "redirect:/login";
        com.example.resolveit.model.User user = userRepository.findByEmail(principal.getName());
        int staffId = user.getId();

        org.springframework.data.domain.Page<com.example.resolveit.model.Complaint> complaintPage = 
            complaintService.getPaginatedStaffComplaints(staffId, page, 10);

        model.addAttribute("complaints", complaintPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", complaintPage.getTotalPages());
        model.addAttribute("pageTitle", "My Tasks");
        model.addAttribute("pageSubtitle", "Manage all grievances currently assigned to you.");
        model.addAttribute("showFilters", true); // Still needed to trigger the dropdown UI in template

        return "staff-list";
    }

    @GetMapping("/staff/all-complaints")
    public String allComplaints(Model model, @RequestParam(defaultValue = "0") int page) {
        org.springframework.data.domain.Page<com.example.resolveit.model.Complaint> complaintPage = 
            complaintService.getPaginatedAllComplaints(page, 10);

        model.addAttribute("complaints", complaintPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", complaintPage.getTotalPages());
        model.addAttribute("pageTitle", "Global Queue");
        model.addAttribute("pageSubtitle", "Full transparent view of all system complaints.");
        return "staff-list";
    }

    @GetMapping("/staff/sla")
    public String slaMonitor(java.security.Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        com.example.resolveit.model.User user = userRepository.findByEmail(principal.getName());
        model.addAttribute("complaints", complaintService.getSLAComplaints(user.getId()));
        model.addAttribute("pageTitle", "SLA Monitor");
        model.addAttribute("pageSubtitle", "CRITICAL: The following items have breached the 48-hour response threshold.");
        model.addAttribute("isSlaView", true);
        model.addAttribute("showFilters", false); // Status not editable here usually
        return "staff-list";
    }


    @GetMapping("/staff/notifications")
    public String notifications(java.security.Principal principal, Model model) {
        model.addAttribute("pageTitle", "Notifications");
        return "staff-notifications";
    }

    @Autowired
    private com.example.resolveit.service.ComplaintHistoryService complaintHistoryService;

    @GetMapping("/staff/timeline")
    public String timeline(java.security.Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        com.example.resolveit.model.User user = userRepository.findByEmail(principal.getName());
        
        java.util.List<Integer> assignedIds = complaintService.findByAssignedStaff(user.getId())
            .stream().map(com.example.resolveit.model.Complaint::getId).toList();
            
        model.addAttribute("histories", complaintHistoryService.getTimelineForStaff(assignedIds));
        model.addAttribute("pageTitle", "Complaint Timeline");
        model.addAttribute("pageSubtitle", "Chronological history of all tickets assigned to you.");
        return "staff-timeline";
    }

    @GetMapping("/staff/activity")
    public String activity(java.security.Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        com.example.resolveit.model.User user = userRepository.findByEmail(principal.getName());
        
        model.addAttribute("histories", complaintHistoryService.getActivityByStaff(user.getName()));
        model.addAttribute("pageTitle", "My Activity Log");
        model.addAttribute("pageSubtitle", "Log of every action and status update you have performed.");
        return "staff-timeline";
    }

    @GetMapping("/staff/chat")
    public String chat(Model model) {
        model.addAttribute("pageTitle", "User Chat");
        return "staff-placeholder";
    }

    @GetMapping("/staff/kb")
    public String kb(Model model) {
        model.addAttribute("pageTitle", "Knowledge Base");
        return "staff-placeholder";
    }

    @Autowired
    private com.example.resolveit.repository.FeedbackRepository feedbackRepository;
    
    @GetMapping("/staff/feedback")
    public String staffFeedback(java.security.Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        
        // Use findAllWithDetails to load ALL feedback with complaint + user relationships
        java.util.List<com.example.resolveit.model.Feedback> feedbacks = feedbackRepository.findAllWithDetails();
        
        // Debug logging
        System.out.println("=== FEEDBACK DEBUG ===");
        System.out.println("Total feedback count: " + feedbacks.size());
        for (com.example.resolveit.model.Feedback f : feedbacks) {
            System.out.println("  ID=" + f.getId() 
                + " | ComplaintID=" + f.getComplaintId() 
                + " | UserID=" + f.getUserId() 
                + " | Rating=" + f.getRating()
                + " | Complaint=" + (f.getComplaint() != null ? f.getComplaint().getTitle() : "NULL")
                + " | User=" + (f.getUser() != null ? f.getUser().getName() : "NULL"));
        }
        System.out.println("=====================");
        
        model.addAttribute("feedbacks", feedbacks);
        
        // Compute average rating
        double avg = feedbacks.isEmpty() ? 0 : feedbacks.stream().mapToInt(f -> f.getRating()).average().getAsDouble();
        model.addAttribute("avgRating", Math.round(avg * 10.0) / 10.0);
        model.addAttribute("totalReviews", feedbacks.size());
        
        return "staff-feedback";
    }
}
