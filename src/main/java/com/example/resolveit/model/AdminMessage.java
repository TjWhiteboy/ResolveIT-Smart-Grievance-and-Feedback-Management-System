package com.example.resolveit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_messages")
public class AdminMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "subject")
    private String subject;

    @Column(name = "priority")
    private String priority; // LOW, MEDIUM, HIGH

    @Column(name = "message", length = 3000)
    private String message;

    @Column(name = "status")
    private String status; // UNREAD, READ, REPLIED

    @Column(name = "admin_reply", length = 3000)
    private String adminReply;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "UNREAD";
        if (priority == null) priority = "MEDIUM";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }
}
