package com.example.resolveit.service;

import com.example.resolveit.model.AdminMessage;
import com.example.resolveit.repository.AdminMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdminMessageService {

    @Autowired
    private AdminMessageRepository adminMessageRepository;

    @Autowired
    private NotificationService notificationService;

    public List<AdminMessage> getAllMessages() {
        return adminMessageRepository.findAll();
    }

    public List<AdminMessage> getMessagesByFilters(String search, String priority, String status, LocalDateTime start, LocalDateTime end) {
        if ("All".equalsIgnoreCase(priority)) priority = null;
        if ("All".equalsIgnoreCase(status)) status = null;
        return adminMessageRepository.findByFilters(search, priority, status, start, end);
    }

    public Optional<AdminMessage> getMessageById(Long id) {
        return adminMessageRepository.findById(id);
    }

    @Transactional
    public void markAsRead(Long id) {
        adminMessageRepository.findById(id).ifPresent(msg -> {
            msg.setRead(true);
            if ("UNREAD".equals(msg.getStatus())) {
                msg.setStatus("READ");
            }
            adminMessageRepository.save(msg);
        });
    }

    @Transactional
    public void markAllAsRead() {
        List<AdminMessage> unread = adminMessageRepository.findByFilters(null, null, "UNREAD", null, null);
        unread.forEach(m -> {
            m.setRead(true);
            m.setStatus("READ");
        });
        adminMessageRepository.saveAll(unread);
    }

    @Transactional
    public void replyToMessage(Long id, String replyText) {
        adminMessageRepository.findById(id).ifPresent(msg -> {
            msg.setAdminReply(replyText);
            msg.setStatus("REPLIED");
            msg.setRepliedAt(LocalDateTime.now());
            msg.setRead(true);
            adminMessageRepository.save(msg);

            // Create notification for user
            notificationService.createNotification(
                msg.getUserId(),
                null,
                "Admin Reply Received",
                "The admin has replied to your message: \"" + msg.getSubject() + "\"",
                "INFO",
                "/messages/history"
            );
        });
    }

    @Transactional
    public void deleteMessage(Long id) {
        adminMessageRepository.deleteById(id);
    }

    public long getUnreadCount() {
        return adminMessageRepository.countByStatus("UNREAD");
    }

    public long getTotalCount() {
        return adminMessageRepository.count();
    }

    public long getHighPriorityCount() {
        return adminMessageRepository.countByPriority("HIGH");
    }

    public long getRepliedCount() {
        return adminMessageRepository.countByStatus("REPLIED");
    }
}
