package com.example.resolveit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.resolveit.repository.ComplaintRepository;

@Controller
public class UserController {

    @Autowired
    private com.example.resolveit.service.UserService userService;

    @Autowired
    private com.example.resolveit.service.ComplaintService complaintService;

    @GetMapping("/my-complaints")
    public String myComplaints(java.security.Principal principal, Model model,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false, defaultValue = "latest") String sort,
                               @RequestParam(defaultValue = "0") int page) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        com.example.resolveit.model.User user = userService.findByEmail(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }

        java.util.Map<String, Long> stats = complaintService.getUserComplaintStats(user.getId());
        
        // Use pagination if no filtering is active for better performance, 
        // otherwise stick to in-memory filtering for now as per previous logic
        org.springframework.data.domain.Page<com.example.resolveit.model.Complaint> complaintPage;
        if ((keyword == null || keyword.isEmpty()) && (status == null || status.isEmpty())) {
            complaintPage = complaintService.getPaginatedUserComplaints(user.getId(), page, 5);
        } else {
            // For filtered results, we'll simulate pagination from the filtered list for now 
            // to maintain the rich filtering/sorting logic we just moved to service.
            java.util.List<com.example.resolveit.model.Complaint> filtered = complaintService.getFilteredUserComplaints(user.getId(), keyword, status, sort);
            int start = Math.min(page * 5, filtered.size());
            int end = Math.min((page + 1) * 5, filtered.size());
            java.util.List<com.example.resolveit.model.Complaint> pagedList = filtered.subList(start, end);
            complaintPage = new org.springframework.data.domain.PageImpl<>(pagedList, org.springframework.data.domain.PageRequest.of(page, 5), filtered.size());
        }

        model.addAttribute("total", stats.get("total"));
        model.addAttribute("pending", stats.get("pending"));
        model.addAttribute("resolved", stats.get("resolved"));
        model.addAttribute("newCount", stats.get("newCount"));
        model.addAttribute("complaints", complaintPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", complaintPage.getTotalPages());
        model.addAttribute("pageTitle", "My Complaints");

        return "my-complaints";
    }
}
