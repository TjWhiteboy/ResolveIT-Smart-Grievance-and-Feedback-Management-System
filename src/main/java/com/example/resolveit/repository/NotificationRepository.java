package com.example.resolveit.repository;

import com.example.resolveit.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
    
    List<Notification> findByRoleOrderByCreatedAtDesc(String role);
    List<Notification> findByRoleAndIsReadFalseOrderByCreatedAtDesc(String role);
    long countByRoleAndIsReadFalse(String role);
    
    void deleteByUserId(Long userId);
}
