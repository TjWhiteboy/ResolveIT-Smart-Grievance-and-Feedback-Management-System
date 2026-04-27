package com.example.resolveit.service;

import com.example.resolveit.model.Complaint;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintHistoryRepository complaintHistoryRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EscalationRepository escalationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Update user profile information.
     */
    @Transactional
    public void updateProfile(Long userId, String name, String phone, String address, String bio) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        user.setBio(bio);
        user.setLastProfileUpdate(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Handle profile picture upload and persistence.
     */
    @Transactional
    public String updateProfilePicture(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Basic validation
        if (file.isEmpty()) throw new RuntimeException("File is empty");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        // 2. Prepare directory
        Path rootPath = Paths.get(uploadDir, "profile").toAbsolutePath();
        if (!Files.exists(rootPath)) {
            Files.createDirectories(rootPath);
        }

        // 3. Delete old file if exists
        String oldPath = user.getProfilePicturePath();
        if (oldPath != null && !oldPath.isEmpty()) {
            try {
                // Remove the leading slash from the path stored in DB to match local filesystem
                String localOldPath = oldPath.startsWith("/") ? oldPath.substring(1) : oldPath;
                Path oldFile = Paths.get(localOldPath).toAbsolutePath();
                Files.deleteIfExists(oldFile);
            } catch (Exception e) {
                // Log warning but continue
                System.err.println("Warning: Could not delete old pfp: " + e.getMessage());
            }
        }

        // 4. Save new file
        String extension = "";
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf("."));
        }
        String newFileName = "pfp_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path destination = rootPath.resolve(newFileName);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        // 5. Update user record
        String relativePath = "/uploads/profile/" + newFileName;
        user.setProfilePicturePath(relativePath);
        userRepository.save(user);

        return relativePath;
    }

    /**
     * Update user account preferences.
     */
    @Transactional
    public void updatePreferences(Long userId, boolean emailAlerts, boolean browserNotifications, boolean darkMode, boolean compactSidebar) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmailAlerts(emailAlerts);
        user.setBrowserNotifications(browserNotifications);
        user.setDarkMode(darkMode);
        user.setCompactSidebar(compactSidebar);
        userRepository.save(user);
    }

    /**
     * Update user password with secure verification.
     */
    @Transactional
    public boolean updatePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    /**
     * Delete user account and all related data (Hard Delete).
     */
    @Transactional
    public void deleteAccount(Long userId) {
        // 1. Find all complaints by the user
        List<Complaint> userComplaints = complaintRepository.findByUserId(userId);
        
        // 2. Delete history and escalations for these complaints
        if (!userComplaints.isEmpty()) {
            complaintHistoryRepository.deleteByComplaintIn(userComplaints);
            escalationRepository.deleteByComplaintIn(userComplaints);
        }
        
        // 3. Delete feedback, notifications, and complaints
        feedbackRepository.deleteByUserId(userId);
        notificationRepository.deleteByUserId(userId);
        complaintRepository.deleteByUserId(userId);
        
        // 4. Finally delete the user
        userRepository.deleteById(userId);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
