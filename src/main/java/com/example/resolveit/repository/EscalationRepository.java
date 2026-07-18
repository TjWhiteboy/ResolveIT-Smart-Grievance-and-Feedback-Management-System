package com.example.resolveit.repository;

import com.example.resolveit.model.Escalation;
import com.example.resolveit.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EscalationRepository extends JpaRepository<Escalation, Long> {
    List<Escalation> findByStatus(String status);
    List<Escalation> findByRaisedBy_Id(Long userId);
    List<Escalation> findByAssignedTo_Id(Long userId);
    Optional<Escalation> findByComplaintId(Long complaintId);
    boolean existsByComplaintIdAndStatusNot(Long complaintId, String status);
    void deleteByComplaintIn(List<Complaint> complaints);
    long countByStatus(String status);
}
