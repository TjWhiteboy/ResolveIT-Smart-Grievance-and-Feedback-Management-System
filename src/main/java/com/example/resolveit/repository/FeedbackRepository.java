package com.example.resolveit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.resolveit.model.Feedback;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    boolean existsByComplaint_Id(int complaintId);

    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.complaint LEFT JOIN FETCH f.user")
    List<Feedback> findAllWithDetails();

    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.complaint c LEFT JOIN FETCH f.user WHERE c.assignedStaff.id = :staffId")
    List<Feedback> findByStaffId(@Param("staffId") int staffId);
}
