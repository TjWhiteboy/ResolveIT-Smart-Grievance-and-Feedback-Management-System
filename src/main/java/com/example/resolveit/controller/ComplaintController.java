package com.example.resolveit.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.resolveit.model.Complaint;
import com.example.resolveit.model.ComplaintHistory;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.ComplaintHistoryRepository;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.UserRepository;
import com.example.resolveit.service.NotificationService;

@Controller
public class ComplaintController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintHistoryRepository complaintHistoryRepository;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/form/complaint")
    public String submitComplaint(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String urgency,
            @RequestParam String visibility,
            @RequestParam(required = false) MultipartFile attachment) {

        // 1. Resolve User
        User user = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            user = userRepository.findByEmail(auth.getName());
        } else if (userId != null && userId > 0) {
            user = userRepository.findById(userId).orElse(null);
        }

        if (user == null) {
            return "redirect:/login.html?error=User session expired. Please log in again.";
        }

        // 2. Create Complaint
        Complaint complaint = new Complaint();
        complaint.setUserId(user.getId());
        complaint.setUserName(user.getName());
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setCategory(category);
        complaint.setUrgency(urgency);
        complaint.setVisibility(visibility);
        complaint.setAnonymous("Anonymous".equalsIgnoreCase(visibility));
        complaint.setStatus("New");
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        
        // Priority logic (optional standard)
        if ("High".equals(urgency)) {
            complaint.setPriority("Critical");
        } else if ("Medium".equals(urgency)) {
            complaint.setPriority("Normal");
        } else {
            complaint.setPriority("Low");
        }

        // 3. Handle File Upload
        if (attachment != null && !attachment.isEmpty()) {
            try {
                Path root = Paths.get(uploadDir);
                if (!Files.exists(root)) {
                    Files.createDirectories(root);
                }
                String filename = UUID.randomUUID().toString() + "_" + attachment.getOriginalFilename();
                Files.copy(attachment.getInputStream(), root.resolve(filename));
                complaint.setAttachmentPath(filename);
            } catch (IOException e) {
                // Log error or handle gracefully
                e.printStackTrace();
            }
        }

        // 4. Save Complaint
        complaint = complaintRepository.save(complaint);

        // 5. Create History Entry
        ComplaintHistory history = new ComplaintHistory();
        history.setComplaint(complaint);
        history.setStatus("New");
        history.setNote("Complaint submitted by " + user.getName());
        history.setDescription("Initial submission of complaint: " + title);
        history.setUpdatedBy(user.getName());
        history.setTimestamp(LocalDateTime.now());
        complaintHistoryRepository.save(history);

        // 6. Notify User
        notificationService.createNotification(
            user.getId(),
            complaint.getId(),
            "Complaint Submitted",
            "Your complaint #" + complaint.getId() + " has been submitted successfully.",
            "INFO"
        );

        return "redirect:/success.html?id=" + complaint.getId() + "&userId=" + user.getId();
    }
}

