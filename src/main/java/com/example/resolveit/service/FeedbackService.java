package com.example.resolveit.service;

import com.example.resolveit.model.Feedback;
import com.example.resolveit.model.Complaint;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.FeedbackRepository;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveFeedback(int userId, int complaintId, int rating, String comment) {
        if (feedbackRepository.existsByComplaint_Id(complaintId)) {
            throw new RuntimeException("Feedback already submitted for this complaint");
        }

        User user = userRepository.findById(userId).orElse(null);
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);

        Feedback f = new Feedback();
        f.setUser(user);
        f.setComplaint(complaint);
        f.setRating(rating);
        f.setComment(comment);
        f.setCreatedAt(LocalDateTime.now());

        feedbackRepository.save(f);
    }
}
