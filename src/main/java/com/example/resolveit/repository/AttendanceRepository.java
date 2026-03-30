package com.example.resolveit.repository;

import com.example.resolveit.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    List<Attendance> findByUser_IdOrderByTimestampDesc(int userId);
    List<Attendance> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
