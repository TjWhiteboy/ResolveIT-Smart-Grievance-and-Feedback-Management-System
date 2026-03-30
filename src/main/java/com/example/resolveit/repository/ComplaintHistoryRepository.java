package com.example.resolveit.repository;

import com.example.resolveit.model.ComplaintHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplaintHistoryRepository extends JpaRepository<ComplaintHistory, Integer> {
    List<ComplaintHistory> findByComplaint_IdOrderByUpdatedAtDesc(int complaintId);
    void deleteByComplaint_Id(int complaintId);
    List<ComplaintHistory> findByComplaint_IdInOrderByUpdatedAtDesc(List<Integer> complaintIds);
    List<ComplaintHistory> findByUpdatedByOrderByUpdatedAtDesc(String updatedBy);
}
