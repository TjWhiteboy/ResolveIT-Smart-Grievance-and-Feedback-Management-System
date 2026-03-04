package com.example.resolveit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.resolveit.model.Complaint;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/form")
public class FormController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @PostMapping("/register")
    public String handleRegister(@RequestParam String name, @RequestParam String email, @RequestParam String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);

        if (user != null) {
            userRepository.save(user);
        }

        return "redirect:/login.html?success=Registration successful! Please login.";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, @RequestParam String password) {
        User user = userRepository.findByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            return "redirect:/complaint.html?user=" + user.getId();
        }

        return "redirect:/login.html?error=Invalid credentials";
    }

    @PostMapping("/complaint")
    public String handleComplaint(@RequestParam String title, @RequestParam String description,
            @RequestParam String category, @RequestParam String urgency,
            @RequestParam(required = false, defaultValue = "0") int userId,
            @RequestParam(required = false, defaultValue = "Public") String visibility,
            @RequestParam(required = false) MultipartFile attachment) {

        Complaint complaint = new Complaint();
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setCategory(category);
        complaint.setUrgency(urgency);
        complaint.setStatus("Open");
        complaint.setUserId(userId);
        complaint.setVisibility(visibility);

        // Handle optional file attachment
        if (attachment != null && !attachment.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String fileName = UUID.randomUUID() + "_" + attachment.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(attachment.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                complaint.setAttachmentPath(filePath.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        complaintRepository.save(complaint);

        return "redirect:/success.html";
    }
}
