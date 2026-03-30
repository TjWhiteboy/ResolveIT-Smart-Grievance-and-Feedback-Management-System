package com.example.resolveit.repository;

import com.example.resolveit.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUser_IdOrderByCreatedAtDesc(int userId);
    long countByUser_IdAndIsReadFalse(int userId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsRead(@org.springframework.data.repository.query.Param("userId") int userId);
}
