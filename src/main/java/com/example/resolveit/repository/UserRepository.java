package com.example.resolveit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.resolveit.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(String email);

}