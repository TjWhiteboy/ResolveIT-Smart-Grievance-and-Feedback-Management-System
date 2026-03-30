package com.example.resolveit.service;

import com.example.resolveit.model.User;
import com.example.resolveit.repository.UserRepository;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.ComplaintHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintHistoryRepository complaintHistoryRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public org.springframework.data.domain.Page<User> getPaginatedUsers(int page, int size) {
        return userRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("name").ascending()));
    }

    public org.springframework.data.domain.Page<User> getPaginatedStaff(int page, int size) {
        return userRepository.findByRole("STAFF", org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("name").ascending()));
    }

    public java.util.List<User> getByRole(String role) {
        return userRepository.findByRole(role);
    }

    public User getById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void deleteUserWithData(int userId) {
        // Delete history for all user's complaints
        complaintRepository.findByUser_Id(userId).forEach(complaint -> {
            complaintHistoryRepository.deleteByComplaint_Id(complaint.getId());
        });
        
        // Delete complaints
        complaintRepository.deleteByUser_Id(userId);
        
        // Delete user
        userRepository.deleteById(userId);
    }
}
