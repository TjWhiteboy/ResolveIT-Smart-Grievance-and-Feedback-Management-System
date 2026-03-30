package com.example.resolveit.service;

import com.example.resolveit.model.ComplaintHistory;
import com.example.resolveit.model.Complaint;
import com.example.resolveit.repository.ComplaintHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ComplaintHistoryService {

    @Autowired
    private ComplaintHistoryRepository historyRepository;

    @Autowired
    private com.example.resolveit.repository.ComplaintRepository complaintRepository;

    public List<ComplaintHistory> getByComplaintId(int complaintId) {
        return historyRepository.findByComplaint_IdOrderByUpdatedAtDesc(complaintId);
    }

    public void addHistory(int complaintId, String status, String note, String updatedBy) {
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        if (complaint != null) {
            ComplaintHistory history = new ComplaintHistory();
            history.setComplaint(complaint);
            history.setStatus(status);
            history.setNote(note);
            history.setUpdatedBy(updatedBy);
            historyRepository.save(history);
        }
    }

    public List<ComplaintHistory> getTimelineForStaff(List<Integer> assignedComplaintIds) {
        if (assignedComplaintIds == null || assignedComplaintIds.isEmpty()) return java.util.Collections.emptyList();
        return historyRepository.findByComplaint_IdInOrderByUpdatedAtDesc(assignedComplaintIds);
    }

    public List<ComplaintHistory> getActivityByStaff(String staffName) {
        return historyRepository.findByUpdatedByOrderByUpdatedAtDesc(staffName);
    }
}
