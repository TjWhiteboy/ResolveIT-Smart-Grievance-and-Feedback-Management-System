package com.example.resolveit.repository;

import com.example.resolveit.model.Complaint;
import com.example.resolveit.model.ComplaintHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplaintHistoryRepository extends JpaRepository<ComplaintHistory, Long> {
    List<ComplaintHistory> findByComplaintOrderByTimestampDesc(Complaint complaint);
    List<ComplaintHistory> findByComplaintIdOrderByTimestampDesc(Long complaintId);

    void deleteByComplaintIn(List<Complaint> complaints);
}
