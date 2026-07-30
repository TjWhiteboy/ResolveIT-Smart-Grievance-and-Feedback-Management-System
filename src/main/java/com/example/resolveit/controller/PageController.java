package com.example.resolveit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/home.html")
    public String homeHtml() {
        return "home";
    }

    @GetMapping("/login.html")
    public String loginHtml() {
        return "login";
    }

    @GetMapping("/register.html")
    public String registerHtml() {
        return "register";
    }

    @GetMapping("/complaint.html")
    public String complaintHtml() {
        return "complaint";
    }

    @GetMapping("/success.html")
    public String successHtml() {
        return "success";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/complaint")
    public String complaintPage() {
        return "complaint";
    }
}