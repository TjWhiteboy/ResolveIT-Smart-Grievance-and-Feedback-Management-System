package com.example.resolveit.service;

import com.example.resolveit.model.Notification;
import com.example.resolveit.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private com.example.resolveit.repository.UserRepository userRepository;

    public List<Notification> getLatestNotifications(Long userId) {
        com.example.resolveit.model.User user = userRepository.findById(userId).orElse(null);
        List<Notification> all = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (user != null) {
            all.addAll(notificationRepository.findByRoleOrderByCreatedAtDesc(user.getRole()));
            all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }
        return all;
    }

    public long getUnreadCount(Long userId) {
        com.example.resolveit.model.User user = userRepository.findById(userId).orElse(null);
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        if (user != null) {
            count += notificationRepository.countByRoleAndIsReadFalse(user.getRole());
        }
        return count;
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        com.example.resolveit.model.User user = userRepository.findById(userId).orElse(null);
        List<Notification> personal = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        personal.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(personal);

        if (user != null) {
            List<Notification> roleBased = notificationRepository.findByRoleAndIsReadFalseOrderByCreatedAtDesc(user.getRole());
            roleBased.forEach(n -> n.setRead(true));
            notificationRepository.saveAll(roleBased);
        }
    }

    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Transactional
    public void createNotification(Long userId, Long complaintId, String title, String message, String type) {
        createNotification(userId, complaintId, title, message, type, complaintId != null ? "/complaint/" + complaintId : null);
    }

    @Transactional
    public void createNotification(Long userId, Long complaintId, String title, String message, String type, String targetUrl) {
        createNotification(userId, null, complaintId, title, message, type, targetUrl);
    }

    @Transactional
    public void createNotification(Long userId, String role, Long complaintId, String title, String message, String type, String targetUrl) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setRole(role);
        n.setComplaintId(complaintId);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setTargetUrl(targetUrl);
        n.setRead(false);
        notificationRepository.save(n);
    }
}
