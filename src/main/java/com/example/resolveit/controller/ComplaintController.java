package com.example.resolveit.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.resolveit.model.Complaint;
import com.example.resolveit.repository.ComplaintRepository;

@RestController
@RequestMapping("/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @GetMapping
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    @PostMapping
    public Complaint createComplaint(@RequestBody Complaint complaint) {
        if(complaint != null) {
            return complaintRepository.save(complaint);
        }
        return null;
    }

    @GetMapping("/{id}")
    public Complaint getComplaintById(@PathVariable int id) {
        return complaintRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Complaint updateComplaint(@PathVariable int id, @RequestBody Complaint complaint) {
        complaint.setId(id);
        return complaintRepository.save(complaint);
    }

    @DeleteMapping("/{id}")
    public void deleteComplaint(@PathVariable int id) {
        complaintRepository.deleteById(id);
    }
}
