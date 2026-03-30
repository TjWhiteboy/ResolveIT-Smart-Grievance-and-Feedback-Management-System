package com.example.resolveit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.Principal;

import com.example.resolveit.model.User;
import com.example.resolveit.service.UserService;
import com.example.resolveit.service.FeedbackService;

@Controller
public class FeedbackController {

    @Autowired
    private UserService userService;

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/feedback/{id}")
    public String feedbackPage(@PathVariable int id, Model model) {
        model.addAttribute("complaintId", id);
        return "feedback";
    }

    @PostMapping("/feedback/submit")
    public String submitFeedback(
            @RequestParam int complaintId,
            @RequestParam int rating,
            @RequestParam String comment,
            Principal principal) {
        
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(principal.getName());

        try {
            feedbackService.saveFeedback(user.getId(), complaintId, rating, comment);
        } catch(Exception e) {
            return "redirect:/my-complaints?error=true";
        }

        return "redirect:/my-complaints?feedbackSuccess=true";
    }
}
