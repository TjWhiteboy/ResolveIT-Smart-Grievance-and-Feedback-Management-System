package com.example.resolveit.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.security.access.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handle404(NoHandlerFoundException ex, Model model) {
        model.addAttribute("error", "The page you are looking for does not exist.");
        model.addAttribute("status", 404);
        return "error/404";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handle403(AccessDeniedException ex, Model model) {
        model.addAttribute("error", "You do not have permission to access this page.");
        model.addAttribute("status", 403);
        return "error/403";
    }

    @ExceptionHandler(Exception.class)
    public String handle500(Exception ex, Model model) {
        model.addAttribute("error", "Something went wrong on our end.");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("status", 500);
        ex.printStackTrace();
        return "error/500";
    }
}
