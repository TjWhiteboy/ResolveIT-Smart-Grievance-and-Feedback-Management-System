package com.example.resolveit.service;

import com.example.resolveit.model.Attendance;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    public Attendance getTodayAttendance(Long userId) {
        return attendanceRepository.findByUserIdAndDate(userId, LocalDate.now());
    }

    public boolean checkIn(User user) {
        LocalDate today = LocalDate.now();
        Attendance existing = attendanceRepository.findByUserIdAndDate(user.getId(), today);
        
        if (existing == null) {
            LocalDateTime now = LocalDateTime.now();
            Attendance a = new Attendance();
            a.setUserId(user.getId());
            a.setUserName(user.getName());
            a.setDate(today);
            a.setCheckInTime(now);
            a.setLoginTime(now);
            a.setStatus("PRESENT");
            a.setNightShift(false); // Default
            a.setHoursWorked(0.0);  // Initial
            attendanceRepository.save(a);
            return true;
        }
        return false;
    }

    public boolean checkOut(User user) {
        LocalDate today = LocalDate.now();
        Attendance existing = attendanceRepository.findByUserIdAndDate(user.getId(), today);
        
        if (existing != null && existing.getCheckOutTime() == null) {
            LocalDateTime now = LocalDateTime.now();
            existing.setCheckOutTime(now);
            existing.setLogoutTime(now);
            
            // Calculate hours worked
            if (existing.getCheckInTime() != null) {
                long minutes = Duration.between(existing.getCheckInTime(), now).toMinutes();
                existing.setHoursWorked(Math.round((minutes / 60.0) * 10.0) / 10.0);
            }
            
            attendanceRepository.save(existing);
            return true;
        }
        return false;
    }
}
