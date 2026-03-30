package com.example.resolveit.service;

import com.example.resolveit.model.Notification;
import com.example.resolveit.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private com.example.resolveit.repository.UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public void notifyUser(int userId, String message) {
        com.example.resolveit.model.User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);
            notificationRepository.save(notification);
        }
    }

    public long getUnreadCount(int userId) {
        return notificationRepository.countByUser_IdAndIsReadFalse(userId);
    }

    public List<Notification> getNotifications(int userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(int notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @org.springframework.transaction.annotation.Transactional
    public void markAllAsRead(int userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
