package com.example.resolveit.controller;

import com.example.resolveit.model.AdminMessage;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.AdminMessageRepository;
import com.example.resolveit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserSupportController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminMessageRepository adminMessageRepository;

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return userRepository.findByEmail(auth.getName());
        }
        return null;
    }

    // ── Help Center ──
    @GetMapping("/help-center")
    public String helpCenter(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        model.addAttribute("userId", user.getId());
        model.addAttribute("userName", user.getName());
        return "help-center";
    }

    // ── Contact Admin (GET) ──
    @GetMapping("/contact-admin")
    public String contactAdminForm(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        model.addAttribute("userId", user.getId());
        model.addAttribute("userName", user.getName());
        return "contact-admin";
    }

    // ── Contact Admin (POST) ──
    @PostMapping("/contact-admin")
    public String submitContactAdmin(
            @RequestParam String subject,
            @RequestParam String message,
            @RequestParam String priority,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";

        AdminMessage msg = new AdminMessage();
        msg.setUserId(user.getId());
        msg.setName(user.getName());
        msg.setEmail(user.getEmail());
        msg.setSubject(subject);
        msg.setMessage(message);
        msg.setPriority(priority.toUpperCase());
        msg.setStatus("UNREAD");
        adminMessageRepository.save(msg);

        redirectAttributes.addFlashAttribute("success", "Your message has been sent to the admin team successfully!");
        return "redirect:/contact-admin";
    }

    @GetMapping("/messages/history")
    public String messageHistory(Model model) {
        User user = getLoggedInUser();
        if (user == null) return "redirect:/login";
        
        List<AdminMessage> messages = adminMessageRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("userId", user.getId());
        model.addAttribute("userName", user.getName());
        model.addAttribute("messages", messages);
        return "message-history";
    }
}
