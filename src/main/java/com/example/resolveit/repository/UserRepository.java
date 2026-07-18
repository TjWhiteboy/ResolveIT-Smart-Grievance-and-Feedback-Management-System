package com.example.resolveit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.resolveit.model.User;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
    List<User> findAllByRole(String role);
    long countByRole(String role);
    
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.role = 'STAFF'")
    List<User> findAllStaff();
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM User u WHERE u.role = 'USER'")
    long countTotalUsers();

}