package com.example.resolveit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role = "USER";

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "profile_picture_path")
    private String profilePicturePath;

    // Preferences
    @Column(name = "email_alerts")
    private Boolean emailAlerts = true;

    @Column(name = "browser_notifications")
    private Boolean browserNotifications = true;

    @Column(name = "dark_mode")
    private Boolean darkMode = false;

    @Column(name = "compact_sidebar")
    private Boolean compactSidebar = false;

    // Access Control
    @Column(name = "enabled")
    private Boolean enabled = true;

    // Audit
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "last_profile_update")
    private LocalDateTime lastProfileUpdate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public Boolean isEmailAlerts() {
        return emailAlerts != null && emailAlerts;
    }

    public void setEmailAlerts(Boolean emailAlerts) {
        this.emailAlerts = emailAlerts;
    }

    public Boolean isBrowserNotifications() {
        return browserNotifications != null && browserNotifications;
    }

    public void setBrowserNotifications(Boolean browserNotifications) {
        this.browserNotifications = browserNotifications;
    }

    public Boolean isDarkMode() {
        return darkMode != null && darkMode;
    }

    public void setDarkMode(Boolean darkMode) {
        this.darkMode = darkMode;
    }

    public Boolean isCompactSidebar() {
        return compactSidebar != null && compactSidebar;
    }

    public void setCompactSidebar(Boolean compactSidebar) {
        this.compactSidebar = compactSidebar;
    }

    public Boolean isEnabled() {
        return enabled != null && enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getLastProfileUpdate() {
        return lastProfileUpdate;
    }

    public void setLastProfileUpdate(LocalDateTime lastProfileUpdate) {
        this.lastProfileUpdate = lastProfileUpdate;
    }
}