package com.example.resolveit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotNull
    @Size(min = 3, message = "Title must be at least 3 characters")
    @Column(name = "title")
    private String title;

    @NotNull
    @Size(min = 10, message = "Description must be at least 10 characters")
    @Column(name = "description", length = 5000)
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "urgency")
    private String urgency;

    @Column(name = "priority")
    private String priority;

    @Column(name = "status")
    private String status;

    @Column(name = "visibility")
    private String visibility;

    @Column(name = "attachment_path")
    private String attachmentPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_name")
    private String userName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "sla_expiry")
    private LocalDateTime slaExpiry;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.slaExpiry == null) {
            this.slaExpiry = this.createdAt.plusHours(48);
        }
    }

    // ── SLA Logic ──

    public boolean isSlaBreached() {
        if (slaExpiry == null) return false;
        // Only active/pending complaints can be breached
        if ("RESOLVED".equalsIgnoreCase(status) || "DENIED".equalsIgnoreCase(status)) return false;
        return LocalDateTime.now().isAfter(slaExpiry);
    }

    public long getHoursToSla() {
        if (slaExpiry == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), slaExpiry).toHours();
    }

    // ── Getters & Setters ──

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(User assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}