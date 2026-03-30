package com.example.resolveit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id", insertable = false, updatable = false)
    private int userId;

    @Column(name = "complaint_id", unique = true, insertable = false, updatable = false)
    private int complaintId;

    @Column(name = "rating")
    private int rating;

    @Column(name = "comment", length = 2000)
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ── JPA Relationships (EAGER to avoid LazyInitializationException in Thymeleaf) ──

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "complaint_id", referencedColumnName = "id")
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // ── Getters & Setters ──

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
