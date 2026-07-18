package com.example.resolveit.repository;

import com.example.resolveit.model.AdminMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminMessageRepository extends JpaRepository<AdminMessage, Long> {

    List<AdminMessage> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByIsReadFalse();

    @Query("SELECT m FROM AdminMessage m WHERE " +
           "(:search IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.message) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:priority IS NULL OR m.priority = :priority) AND " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:start IS NULL OR m.createdAt >= :start) AND " +
           "(:end IS NULL OR m.createdAt <= :end) " +
           "ORDER BY m.createdAt DESC")
    List<AdminMessage> findByFilters(
            @Param("search") String search,
            @Param("priority") String priority,
            @Param("status") String status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    long countByStatus(String status);
    
    long countByPriority(String priority);
}
