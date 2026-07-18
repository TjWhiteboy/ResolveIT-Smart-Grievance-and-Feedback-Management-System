package com.example.resolveit.controller;

import com.example.resolveit.model.AdminMessage;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.NotificationRepository;
import com.example.resolveit.repository.UserRepository;
import com.example.resolveit.service.AdminMessageService;
import com.example.resolveit.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/messages")
public class AdminMessageController {

    @Autowired
    private AdminMessageService adminMessageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SystemSettingService systemSettingService;

    private User getLoggedInAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User user = userRepository.findByEmail(auth.getName());
            if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
                return user;
            }
        }
        return null;
    }

    private void addCommonAttrs(Model model, User admin, String activePage) {
        model.addAttribute("user", admin);
        model.addAttribute("userName", admin.getName());
        model.addAttribute("activePage", activePage);
        model.addAttribute("appName", systemSettingService.getSetting("APP_NAME", "ResolveIT"));
        model.addAttribute("unreadCount", notificationRepository.countByUserIdAndIsReadFalse(admin.getId()));
    }

    @GetMapping
    public String listMessages(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Model model) {

        User admin = getLoggedInAdmin();
        if (admin == null) return "redirect:/login";

        addCommonAttrs(model, admin, "messages");
        List<AdminMessage> messages = adminMessageService.getMessagesByFilters(search, priority, status, start, end);

        model.addAttribute("messages", messages);
        model.addAttribute("totalMessages", adminMessageService.getTotalCount());
        model.addAttribute("unreadMessages", adminMessageService.getUnreadCount());
        model.addAttribute("highPriority", adminMessageService.getHighPriorityCount());
        model.addAttribute("repliedMessages", adminMessageService.getRepliedCount());

        // For filters
        model.addAttribute("search", search);
        model.addAttribute("currentPriority", priority);
        model.addAttribute("currentStatus", status);

        return "admin/messages";
    }

    @GetMapping("/{id}")
    public String viewMessage(@PathVariable Long id, Model model) {
        User admin = getLoggedInAdmin();
        if (admin == null) return "redirect:/login";

        addCommonAttrs(model, admin, "messages");
        adminMessageService.getMessageById(id).ifPresent(msg -> {
            adminMessageService.markAsRead(id);
            model.addAttribute("message", msg);
        });

        return "admin/message-view";
    }

    @PostMapping("/reply/{id}")
    public String replyToMessage(@PathVariable Long id, @RequestParam String replyText, RedirectAttributes redirectAttributes) {
        User admin = getLoggedInAdmin();
        if (admin == null) return "redirect:/login";

        adminMessageService.replyToMessage(id, replyText);
        redirectAttributes.addFlashAttribute("success", "Reply sent successfully!");
        return "redirect:/admin/messages/" + id;
    }

    @PostMapping("/read/{id}")
    public String markAsRead(@PathVariable Long id) {
        User admin = getLoggedInAdmin();
        if (admin == null) return "redirect:/login";

        adminMessageService.markAsRead(id);
        return "redirect:/admin/messages";
    }

    @PostMapping("/read-all")
    public String markAllAsRead() {
        User admin = getLoggedInAdmin();
        if (admin == null) return "redirect:/login";

        adminMessageService.markAllAsRead();
        return "redirect:/admin/messages";
    }

    @PostMapping("/delete/{id}")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User admin = getLoggedInAdmin();
        if (admin == null) return "redirect:/login";

        adminMessageService.deleteMessage(id);
        redirectAttributes.addFlashAttribute("success", "Message deleted successfully!");
        return "redirect:/admin/messages";
    }
}
