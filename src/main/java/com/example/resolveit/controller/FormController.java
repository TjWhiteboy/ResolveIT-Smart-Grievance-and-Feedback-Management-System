package com.example.resolveit.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.resolveit.model.Complaint;
import com.example.resolveit.model.ComplaintHistory;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.ComplaintHistoryRepository;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.UserRepository;

@Controller
@RequestMapping("/form")
public class FormController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintHistoryRepository complaintHistoryRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ── Submit Complaint ──────────────────────────────────────────────────
    @PostMapping("/complaint")
    public String handleComplaint(
            @jakarta.validation.Valid Complaint complaint,
            org.springframework.validation.BindingResult result,
            java.security.Principal principal,
            @RequestParam(required = false) MultipartFile attachment,
            org.springframework.ui.Model model) {

        if (result.hasErrors()) {
            return "complaint";
        }

        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }

        complaint.setStatus("New");
        if (complaint.getVisibility() == null) {
            complaint.setVisibility("Public");
        }
        if (complaint.getPriority() == null) {
            complaint.setPriority("Low");
        }
        complaint.setUser(user);
        complaint.setUserName(user.getName());
        complaint.setCreatedAt(LocalDateTime.now());

        // ── Handle file upload ──────────────────────────────────────────
        if (attachment != null && !attachment.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                Files.createDirectories(uploadPath);

                String originalName = attachment.getOriginalFilename();
                String extension = "";
                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }
                String savedName = UUID.randomUUID().toString() + extension;

                Path filePath = uploadPath.resolve(savedName);
                Files.copy(attachment.getInputStream(), filePath);

                complaint.setAttachmentPath(uploadDir + File.separator + savedName);

            } catch (IOException e) {
                System.err.println("File upload failed: " + e.getMessage());
            }
        }

        Complaint saved = complaintRepository.save(complaint);

        // Save initial history entry
        ComplaintHistory history = new ComplaintHistory();
        history.setComplaint(saved);
        history.setStatus("New");
        history.setNote("Complaint submitted");
        history.setUpdatedBy(user.getName());
        complaintHistoryRepository.save(history);

        return "redirect:/success.html";
    }
}
