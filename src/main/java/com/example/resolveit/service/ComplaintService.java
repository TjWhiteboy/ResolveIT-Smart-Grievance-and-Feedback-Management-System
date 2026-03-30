package com.example.resolveit.service;

import com.example.resolveit.model.Complaint;
import com.example.resolveit.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintHistoryService historyService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public java.util.List<com.example.resolveit.model.Complaint> findByUserId(int userId) {
        return complaintRepository.findByUser_Id(userId);
    }

    public java.util.List<com.example.resolveit.model.Complaint> findByAssignedStaff(int staffId) {
        return complaintRepository.findByAssignedStaff_Id(staffId);
    }

    public java.util.List<com.example.resolveit.model.Complaint> findAll() {
        return complaintRepository.findAll();
    }

    public java.util.List<com.example.resolveit.model.Complaint> getAll() {
        return complaintRepository.findAll();
    }

    public java.util.List<com.example.resolveit.model.Complaint> getByStatus(String status) {
        return complaintRepository.findByStatus(status);
    }

    public java.util.List<com.example.resolveit.model.Complaint> search(String keyword) {
        return complaintRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }

    public java.util.List<com.example.resolveit.model.Complaint> findByTitleContaining(String keyword) {
        return complaintRepository.findByTitleContaining(keyword);
    }

    public Complaint getById(int id) {
        return complaintRepository.findById(id).orElse(null);
    }

    @Autowired
    private com.example.resolveit.repository.UserRepository userRepository;

    public void updateStatus(int complaintId, String status, String note, String updatedBy) {
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        if (complaint != null) {
            complaint.setStatus(status);
            if ("Resolved".equals(status)) {
                complaint.setResolvedAt(LocalDateTime.now());
            }
            complaintRepository.save(complaint);

            // Log history entry
            historyService.addHistory(complaintId, status, note, updatedBy);

            // Notify user
            if (complaint.getUser() != null) {
                notificationService.notifyUser(complaint.getUser().getId(), "Your complaint status has been updated to: " + status);
            }
            
            // WebSocket update
            messagingTemplate.convertAndSend("/topic/updates", "Complaint #" + complaintId + " status updated to " + status);
        }
    }

    public void assignStaff(int complaintId, int staffId, String updatedBy) {
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        com.example.resolveit.model.User staff = userRepository.findById(staffId).orElse(null);
        if (complaint != null && staff != null) {
            complaint.setAssignedStaff(staff);
            complaintRepository.save(complaint);

            // Log history entry
            historyService.addHistory(complaintId, complaint.getStatus() != null ? complaint.getStatus() : "New", "Staff assigned", updatedBy);

            // Notify user
            if (complaint.getUser() != null) {
                notificationService.notifyUser(complaint.getUser().getId(), "A staff member has been assigned to your complaint.");
            }
            
            // WebSocket update
            messagingTemplate.convertAndSend("/topic/updates", "Complaint #" + complaintId + " has been assigned to staff");
        }
    }

    public int countByUser(int userId) {
        return complaintRepository.countByUser_Id(userId);
    }

    public int countByStatus(int userId, String status) {
        return complaintRepository.countByUser_IdAndStatus(userId, status);
    }

    public java.util.List<Complaint> getFilteredUserComplaints(int userId, String keyword, String status, String sort) {
        java.util.List<Complaint> complaints = complaintRepository.findByUser_Id(userId);
        java.util.List<Complaint> filtered = new java.util.ArrayList<>(complaints);

        if (keyword != null && !keyword.isEmpty()) {
            final String kw = keyword.toLowerCase();
            filtered = filtered.stream()
                .filter(c -> (c.getTitle() != null && c.getTitle().toLowerCase().contains(kw)) ||
                             (c.getDescription() != null && c.getDescription().toLowerCase().contains(kw)))
                .collect(java.util.stream.Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
            filtered = filtered.stream()
                .filter(c -> status.equalsIgnoreCase(c.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        }

        if ("oldest".equals(sort)) {
            filtered.sort(java.util.Comparator.comparing(Complaint::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        } else if ("status".equals(sort)) {
            filtered.sort(java.util.Comparator.comparing(Complaint::getStatus, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        } else {
            // latest
            filtered.sort(java.util.Comparator.comparing(Complaint::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));
        }

        return filtered;
    }

    public java.util.Map<String, Long> getUserComplaintStats(int userId) {
        java.util.List<Complaint> complaints = complaintRepository.findByUser_Id(userId);
        
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", (long) complaints.size());
        stats.put("pending", complaints.stream().filter(c -> "Pending".equalsIgnoreCase(c.getStatus()) || "Under Review".equalsIgnoreCase(c.getStatus())).count());
        stats.put("resolved", complaints.stream().filter(c -> "Resolved".equalsIgnoreCase(c.getStatus()) || "Solved".equalsIgnoreCase(c.getStatus())).count());
        stats.put("newCount", complaints.stream().filter(c -> "New".equalsIgnoreCase(c.getStatus()) || "Open".equalsIgnoreCase(c.getStatus())).count());
        
        return stats;
    }

    public org.springframework.data.domain.Page<Complaint> getPaginatedUserComplaints(int userId, int page, int size) {
        return complaintRepository.findByUser_Id(userId, org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending()));
    }

    public java.util.Map<String, Object> getDashboardStats(int userId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        int total = countByUser(userId);
        int resolved = countByStatus(userId, "Solved") + countByStatus(userId, "Resolved");
        int pending = countByStatus(userId, "Pending") + countByStatus(userId, "In Progress") + countByStatus(userId, "Under Review");
        double avgTime = getAverageResolutionTime(userId);
        java.util.List<Complaint> recent = getRecent(userId);

        stats.put("total", total);
        stats.put("resolved", resolved);
        stats.put("pending", pending);
        stats.put("avgTime", avgTime);
        stats.put("recent", recent);
        
        return stats;
    }

    public java.util.List<Complaint> getRecent(int userId) {
        return complaintRepository.findTop5ByUser_IdOrderByCreatedAtDesc(userId);
    }

    public double getAverageResolutionTime(int userId) {
        java.util.List<Complaint> resolved = complaintRepository.findByUser_Id(userId).stream()
            .filter(c -> "Solved".equals(c.getStatus()) || "Resolved".equals(c.getStatus()))
            .filter(c -> c.getResolvedAt() != null)
            .toList();
        if (resolved.isEmpty()) return 0.0;
        
        double totalDays = 0;
        for (Complaint c : resolved) {
            long days = java.time.Duration.between(c.getCreatedAt(), c.getResolvedAt()).toDays();
            totalDays += (days == 0 ? 1 : days);
        }
        return Math.round((totalDays / resolved.size()) * 10.0) / 10.0;
    }

    public java.util.List<Complaint> getSLAComplaints(int staffId) {
        return complaintRepository.findBreached(staffId, java.util.Arrays.asList("Resolved", "Solved", "Denied"), java.time.LocalDateTime.now());
    }

    public int getSLAComplaintsCount(int staffId) {
        return complaintRepository.countBreached(staffId, java.util.Arrays.asList("Resolved", "Solved", "Denied"), java.time.LocalDateTime.now());
    }

    public double getStaffAverageResolutionTime(int staffId) {
        java.util.List<Complaint> resolved = complaintRepository.findByAssignedStaff_Id(staffId).stream()
            .filter(c -> "Solved".equals(c.getStatus()) || "Resolved".equals(c.getStatus()))
            .filter(c -> c.getResolvedAt() != null)
            .toList();
        if (resolved.isEmpty()) return 0.0;
        
        double totalDays = 0;
        for (Complaint c : resolved) {
            long days = java.time.Duration.between(c.getCreatedAt(), c.getResolvedAt()).toDays();
            totalDays += (days == 0 ? 1 : days);
        }
        return Math.round((totalDays / resolved.size()) * 10.0) / 10.0;
    }
    
    public org.springframework.data.domain.Page<Complaint> getPaginatedStaffComplaints(int staffId, int page, int size) {
        return complaintRepository.findByAssignedStaff_Id(staffId, org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending()));
    }

    public org.springframework.data.domain.Page<Complaint> getPaginatedAllComplaints(int page, int size) {
        return complaintRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending()));
    }

    public java.util.Map<String, Object> getStaffDashboardStats(int staffId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        int total = complaintRepository.countByAssignedStaff_Id(staffId);
        int resolved = complaintRepository.countByAssignedStaff_IdAndStatus(staffId, "Solved") + complaintRepository.countByAssignedStaff_IdAndStatus(staffId, "Resolved");
        int pending = complaintRepository.countByAssignedStaff_IdAndStatus(staffId, "Pending") + complaintRepository.countByAssignedStaff_IdAndStatus(staffId, "In Progress") + complaintRepository.countByAssignedStaff_IdAndStatus(staffId, "Open");
        double avgTime = getStaffAverageResolutionTime(staffId);
        int slaViolations = getSLAComplaintsCount(staffId);

        stats.put("total", total);
        stats.put("resolved", resolved);
        stats.put("pending", pending);
        stats.put("avgTime", avgTime);
        stats.put("slaViolations", slaViolations);
        return stats;
    }

    public int getGlobalSLABreachesCount() {
        return complaintRepository.countGlobalBreached(java.util.Arrays.asList("Resolved", "Solved", "Denied"), java.time.LocalDateTime.now());
    }
}
