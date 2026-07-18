package com.example.resolveit.service;

import com.example.resolveit.model.Escalation;
import com.example.resolveit.model.Complaint;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.EscalationRepository;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class EscalationService {

    @Autowired
    private EscalationRepository escalationRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public boolean existsActiveEscalation(Long complaintId) {
        return escalationRepository.existsByComplaintIdAndStatusNot(complaintId, "RESOLVED");
    }

    @Transactional
    public Escalation raiseEscalation(Complaint complaint, User staff, String reason, String comment, String severity) {
        if (existsActiveEscalation(complaint.getId())) {
            throw new IllegalStateException("An active escalation already exists for this complaint.");
        }

        Escalation escalation = new Escalation();
        escalation.setComplaint(complaint);
        escalation.setRaisedBy(staff);
        escalation.setReason(reason);
        escalation.setComment(comment);
        escalation.setSeverity(severity);
        escalation.setStatus("OPEN");
        Escalation saved = escalationRepository.save(escalation);

        // Notify all Admins
        List<User> admins = userRepository.findAllByRole("ADMIN");
        for (User admin : admins) {
            notificationService.createNotification(
                admin.getId(),
                complaint.getId(),
                "Escalation Raised",
                "CRITICAL: Complaint #" + complaint.getId() + " escalated by " + staff.getName() + " (" + severity + ")",
                "SLA_ALERT"
            );
        }

        // Notify User
        notificationService.createNotification(
            complaint.getUserId(),
            complaint.getId(),
            "Complaint Escalated",
            "Your complaint #" + complaint.getId() + " has been escalated for priority handling.",
            "WARNING"
        );

        return saved;
    }

    @Transactional
    public boolean processAutoEscalation(List<Complaint> overdue, User triggerUser) {
        boolean modified = false;
        for (Complaint c : overdue) {
            if (!existsActiveEscalation(c.getId())) {
                try {
                    raiseEscalation(c, triggerUser, "Automatic SLA Breach", 
                        "System automatically escalated this complaint as it exceeded the 48-hour recovery window.", 
                        "HIGH");
                    modified = true;
                } catch (Exception e) {
                    // Log or handle individual failure
                }
            }
        }
        return modified;
    }

    public List<Escalation> getAllEscalations() {
        return escalationRepository.findAll();
    }

    public List<Escalation> getEscalationsByStatus(String status) {
        return escalationRepository.findByStatus(status);
    }

    public Optional<Escalation> getEscalationById(Long id) {
        return escalationRepository.findById(id);
    }

    public long getCountByStatus(String status) {
        return escalationRepository.countByStatus(status);
    }

    @Transactional
    public Escalation updateStatus(Long id, String status, String adminComment) {
        Escalation escalation = escalationRepository.findById(id).orElseThrow();
        escalation.setStatus(status);
        if (adminComment != null) {
            escalation.setAdminComment(adminComment);
        }

        // Notify Staff Member
        notificationService.createNotification(
            escalation.getRaisedBy().getId(),
            escalation.getComplaint().getId(),
            "Escalation Update",
            "Your escalation for Complaint #" + escalation.getComplaint().getId() + " is now " + status,
            "STATUS_UPDATE"
        );

        // Notify User
        notificationService.createNotification(
            escalation.getComplaint().getUserId(),
            escalation.getComplaint().getId(),
            "Escalation Handled",
            "Your escalated complaint #" + escalation.getComplaint().getId() + " has been reviewed by admin.",
            "INFO"
        );

        // Sync with Complaint if Resolved
        if ("RESOLVED".equalsIgnoreCase(status)) {
            Complaint c = escalation.getComplaint();
            c.setStatus("Resolved");
            complaintRepository.save(c);
        }

        return escalationRepository.save(escalation);
    }

    @Transactional
    public Escalation assignStaff(Long id, User staff) {
        Escalation escalation = escalationRepository.findById(id).orElseThrow();
        escalation.setAssignedTo(staff);
        
        // Sync Complaint Assignment
        Complaint c = escalation.getComplaint();
        c.setAssignedStaff(staff.getId());
        complaintRepository.save(c);

        return escalationRepository.save(escalation);
    }

    public Escalation addAdminComment(Long id, String comment) {
        Escalation escalation = escalationRepository.findById(id).orElseThrow();
        escalation.setAdminComment(comment);
        return escalationRepository.save(escalation);
    }
}
