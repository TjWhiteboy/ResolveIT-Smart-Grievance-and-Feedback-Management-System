package com.example.resolveit.repository;

import com.example.resolveit.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Feedback findByComplaintId(Long complaintId);

    List<Feedback> findByUserId(Long userId);

    boolean existsByComplaintId(Long complaintId);

    void deleteByUserId(Long userId);

    @Query("SELECT f FROM Feedback f WHERE " +
           "(:status IS NULL OR :status = '' OR f.status = :status) AND " +
           "(:rating IS NULL OR :rating = 0 OR f.rating = :rating) AND " +
           "(:staffId IS NULL OR :staffId = 0L OR f.staffId = :staffId) AND " +
           "(:startDate IS NULL OR f.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR f.createdAt <= :endDate) AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(f.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.comment) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Feedback> findWithFilters(
            @Param("status") String status, 
            @Param("rating") Integer rating, 
            @Param("staffId") Long staffId, 
            @Param("keyword") String keyword, 
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            Sort sort);

    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double getAverageRating();

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.rating >= 4")
    Long countPositiveReviews();

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.rating <= 2")
    Long countNegativeReviews();

    @Query("SELECT f.rating, COUNT(f) FROM Feedback f GROUP BY f.rating ORDER BY f.rating DESC")
    List<Object[]> getRatingDistribution();

    @Query("SELECT f.staffId, COUNT(f), AVG(f.rating), " +
           "SUM(CASE WHEN f.rating >= 4 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN f.rating <= 2 THEN 1 ELSE 0 END) " +
           "FROM Feedback f WHERE f.staffId IS NOT NULL GROUP BY f.staffId")
    List<Object[]> getStaffPerformance();

    @Query("SELECT FUNCTION('DATE_FORMAT', f.createdAt, '%Y-%m'), COUNT(f) " +
           "FROM Feedback f GROUP BY FUNCTION('DATE_FORMAT', f.createdAt, '%Y-%m') " +
           "ORDER BY FUNCTION('DATE_FORMAT', f.createdAt, '%Y-%m') DESC")
    List<Object[]> getMonthlyFeedbackTrends();
}
