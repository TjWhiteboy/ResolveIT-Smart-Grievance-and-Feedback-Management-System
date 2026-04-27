package com.example.resolveit.repository;

import com.example.resolveit.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Attendance findByUserIdAndDate(Long userId, LocalDate date);
    List<Attendance> findByDateOrderByCheckInTimeDesc(LocalDate date);
    List<Attendance> findAllByOrderByDateDescCheckInTimeDesc();
    List<Attendance> findByUserId(Long userId);
    long countByDateAndStatus(LocalDate date, String status);
}
