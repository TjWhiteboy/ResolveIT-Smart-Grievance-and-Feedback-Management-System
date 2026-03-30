package com.example.resolveit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.resolveit.model.Complaint;
import java.util.List;
import java.util.Collection;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {

    org.springframework.data.domain.Page<Complaint> findByUser_Id(int userId, org.springframework.data.domain.Pageable pageable);
    
    java.util.List<Complaint> findByUser_Id(int userId);

    List<Complaint> findByAssignedStaff_Id(int staffId);
    org.springframework.data.domain.Page<Complaint> findByAssignedStaff_Id(int staffId, org.springframework.data.domain.Pageable pageable);

    List<Complaint> findByStatus(String status);

    List<Complaint> findByTitleContaining(String keyword);

    List<Complaint> findByTitleContainingOrDescriptionContaining(String titleKeyword, String descKeyword);

    int countByUser_Id(int userId);
    int countByUser_IdAndStatus(int userId, String status);
    List<Complaint> findTop5ByUser_IdOrderByCreatedAtDesc(int userId);

    void deleteByUser_Id(int userId);

    List<Complaint> findByAssignedStaff_IdAndStatus(int staffId, String status);
    int countByAssignedStaff_Id(int staffId);
    int countByAssignedStaff_IdAndStatus(int staffId, String status);
    @Query("SELECT c FROM Complaint c WHERE c.assignedStaff.id = :staffId AND c.status NOT IN :statuses AND c.slaExpiry < :now")
    List<Complaint> findBreached(@Param("staffId") int staffId, @Param("statuses") Collection<String> statuses, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.assignedStaff.id = :staffId AND c.status NOT IN :statuses AND c.slaExpiry < :now")
    int countBreached(@Param("staffId") int staffId, @Param("statuses") Collection<String> statuses, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status NOT IN :statuses AND c.slaExpiry < :now")
    int countGlobalBreached(@Param("statuses") Collection<String> statuses, @Param("now") LocalDateTime now);
}
