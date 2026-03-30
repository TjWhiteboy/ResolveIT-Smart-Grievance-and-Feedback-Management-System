package com.example.resolveit.service;

import com.example.resolveit.model.Attendance;
import com.example.resolveit.model.User;
import com.example.resolveit.repository.AttendanceRepository;
import com.example.resolveit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    public void markAttendance(int userId, String status) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Basic validation: Don't allow same status twice in a row
            List<Attendance> lastRecord = attendanceRepository.findByUser_IdOrderByTimestampDesc(userId);
            if (!lastRecord.isEmpty() && lastRecord.get(0).getStatus().equalsIgnoreCase(status)) {
                return; // Or throw exception
            }

            Attendance attendance = new Attendance();
            attendance.setUser(user);
            attendance.setStatus(status.toUpperCase());
            attendance.setTimestamp(LocalDateTime.now());
            attendanceRepository.save(attendance);
        }
    }

    public Attendance getLastAttendance(int userId) {
        List<Attendance> records = attendanceRepository.findByUser_IdOrderByTimestampDesc(userId);
        return records.isEmpty() ? null : records.get(0);
    }

    public List<Attendance> getAttendanceForUser(int userId) {
        return attendanceRepository.findByUser_IdOrderByTimestampDesc(userId);
    }

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }
}
