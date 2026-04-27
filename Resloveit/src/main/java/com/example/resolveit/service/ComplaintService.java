package com.example.resolveit.service;

import com.example.resolveit.model.Complaint;
import com.example.resolveit.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private SlaService slaService;

    public Complaint getById(Long id) {
        return complaintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + id));
    }

    public boolean isResolved(Complaint c) {
        if (c == null || c.getStatus() == null) return false;
        String s = c.getStatus().toUpperCase();
        return s.equals("RESOLVED") || s.equals("DENIED") || s.equals("CLOSED");
    }

    public boolean isOverdue(Complaint c) {
        return "OVERDUE".equals(slaService.getSlaStatus(c));
    }

    public List<Complaint> getOverdueComplaintsForStaff(Long staffId) {
        List<Complaint> tasks = complaintRepository.findByAssignedStaff(staffId);
        return tasks.stream()
                .filter(this::isOverdue)
                .collect(Collectors.toList());
    }

    public String getSLACategory(Complaint c) {
        return slaService.getSlaStatus(c);
    }
}
