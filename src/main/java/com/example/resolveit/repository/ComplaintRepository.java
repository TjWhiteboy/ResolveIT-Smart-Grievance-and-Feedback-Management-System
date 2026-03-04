package com.example.resolveit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.resolveit.model.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {

}
