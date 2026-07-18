package com.example.resolveit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.resolveit.model.Complaint;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByUserId(Long userId);

    List<Complaint> findByAssignedStaff(Long staffId);

    List<Complaint> findByStatus(String status);

    List<Complaint> findByTitleContaining(String keyword);

    // Search + filter for user dashboard
    List<Complaint> findByUserIdAndStatus(Long userId, String status);

    List<Complaint> findByUserIdAndTitleContainingIgnoreCase(Long userId, String keyword);

    List<Complaint> findByUserIdAndStatusAndTitleContainingIgnoreCase(Long userId, String status, String keyword);

    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Complaint c WHERE c.userId = :userId " +
            "AND (:status IS NULL OR c.status = :status OR :status = '') " +
            "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:startDate IS NULL OR c.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR c.createdAt <= :endDate) " +
            "ORDER BY c.createdAt DESC")
    List<Complaint> findByUserIdAndFilters(
            @org.springframework.data.repository.query.Param("userId") Long userId,
            @org.springframework.data.repository.query.Param("status") String status,
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);
    List<Complaint> findByStatusNotAndStatusNotAndStatusNotAndCreatedAtBefore(
            String status1, String status2, String status3, java.time.LocalDateTime dateTime);

    void deleteByUserId(Long userId);

    // ── Analytics Queries ──
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = :status")
    long countByStatus(@org.springframework.data.repository.query.Param("status") String status);

    @org.springframework.data.jpa.repository.Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category")
    List<Object[]> countByCategory();

    @org.springframework.data.jpa.repository.Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countByStatusGrouped();

    @org.springframework.data.jpa.repository.Query("SELECT FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m'), COUNT(c) FROM Complaint c GROUP BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m') ORDER BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m') DESC")
    List<Object[]> countByMonth();

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Complaint c WHERE " +
            "(:status IS NULL OR c.status = :status OR :status = '') " +
            "AND (:category IS NULL OR c.category = :category OR :category = '') " +
            "AND (:urgency IS NULL OR c.urgency = :urgency OR :urgency = '') " +
            "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY c.createdAt DESC")
    List<Complaint> findAllWithFilters(
            @org.springframework.data.repository.query.Param("status") String status,
            @org.springframework.data.repository.query.Param("category") String category,
            @org.springframework.data.repository.query.Param("urgency") String urgency,
            @org.springframework.data.repository.query.Param("keyword") String keyword);

    List<Complaint> findTop10ByOrderByCreatedAtDesc();
}

