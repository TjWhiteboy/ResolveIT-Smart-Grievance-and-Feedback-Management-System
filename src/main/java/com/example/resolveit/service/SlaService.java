package com.example.resolveit.service;

import com.example.resolveit.model.Complaint;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class SlaService {

    /**
     * Determines the SLA status based on the submission date (createdAt).
     * Rule:
     * - Status RESOLVED/DENIED/CLOSED -> "COMPLETED"
     * - hours > 48 -> "OVERDUE"
     * - hours >= 36 -> "DUE_SOON"
     * - else -> "ON_TRACK"
     */
    public String getSlaStatus(Complaint complaint) {
        if (complaint == null || complaint.getCreatedAt() == null) return "ON_TRACK";
        
        // Skip completed complaints
        String s = complaint.getStatus() != null ? complaint.getStatus().toUpperCase() : "";
        if (s.equals("RESOLVED") || s.equals("DENIED") || s.equals("CLOSED")) {
            return "COMPLETED";
        }

        long hours = getElapsedHours(complaint);

        if (hours > 48) {
            return "OVERDUE";
        } else if (hours >= 36) {
            return "DUE_SOON";
        } else {
            return "ON_TRACK";
        }
    }

    public long getElapsedHours(Complaint complaint) {
        if (complaint == null || complaint.getCreatedAt() == null) return 0;
        
        LocalDateTime start = complaint.getCreatedAt();
        LocalDateTime end = (isResolved(complaint) && complaint.getUpdatedAt() != null) 
                            ? complaint.getUpdatedAt() : LocalDateTime.now();
        
        return Duration.between(start, end).toHours();
    }
    
    private boolean isResolved(Complaint c) {
        if (c == null || c.getStatus() == null) return false;
        String s = c.getStatus().toUpperCase();
        return s.equals("RESOLVED") || s.equals("DENIED") || s.equals("CLOSED");
    }
}
