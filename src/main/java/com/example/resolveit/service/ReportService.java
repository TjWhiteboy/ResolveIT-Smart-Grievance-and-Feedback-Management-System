package com.example.resolveit.service;

import com.example.resolveit.model.Complaint;
import com.example.resolveit.model.User;
import com.example.resolveit.model.Attendance;
import com.example.resolveit.model.Feedback;
import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.UserRepository;
import com.example.resolveit.repository.AttendanceRepository;
import com.example.resolveit.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired private ComplaintRepository complaintRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private FeedbackRepository feedbackRepo;
    @Autowired private AnalyticsService analyticsService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String generateComplaintReport() {
        List<Complaint> complaints = complaintRepo.findAll();
        StringBuilder csv = new StringBuilder("ID,User,Title,Category,Urgency,Status,Created At,Assigned Staff\n");
        for (Complaint c : complaints) {
            csv.append(c.getId()).append(",")
               .append("\"").append(safe(c.getUserName())).append("\",")
               .append("\"").append(safe(c.getTitle())).append("\",")
               .append(safe(c.getCategory())).append(",")
               .append(safe(c.getUrgency())).append(",")
               .append(safe(c.getStatus())).append(",")
               .append(c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_FORMAT) : "N/A").append(",")
               .append(c.getAssignedStaff() != null ? c.getAssignedStaff() : "Unassigned").append("\n");
        }
        return csv.toString();
    }

    public String generateUserReport() {
        List<User> users = userRepo.findAllByRole("USER");
        StringBuilder csv = new StringBuilder("ID,Name,Email,Joined At,Status\n");
        for (User u : users) {
            csv.append(u.getId()).append(",")
               .append("\"").append(safe(u.getName())).append("\",")
               .append(safe(u.getEmail())).append(",")
               .append(u.getCreatedAt() != null ? u.getCreatedAt().format(DATE_FORMAT) : "N/A").append(",")
               .append(u.isEnabled() ? "Active" : "Inactive").append("\n");
        }
        return csv.toString();
    }

    public String generateAttendanceReport() {
        List<Attendance> records = attendanceRepo.findAllByOrderByDateDescCheckInTimeDesc();
        StringBuilder csv = new StringBuilder("Staff Name,Date,Check In,Check Out,Status,Hours Worked\n");
        for (Attendance a : records) {
            csv.append("\"").append(safe(a.getUserName())).append("\",")
               .append(a.getDate()).append(",")
               .append(a.getCheckInTime() != null ? a.getCheckInTime().format(DATE_FORMAT) : "").append(",")
               .append(a.getCheckOutTime() != null ? a.getCheckOutTime().format(DATE_FORMAT) : "").append(",")
               .append(safe(a.getStatus())).append(",")
               .append(a.getHoursWorked()).append("\n");
        }
        return csv.toString();
    }

    public String generateStaffReport() {
        List<Map<String, Object>> stats = analyticsService.getStaffWorkload();
        StringBuilder csv = new StringBuilder("Staff Name,Total Complaints,Resolved,Pending,Success Rate %\n");
        for (Map<String, Object> s : stats) {
            csv.append("\"").append(safe(s.get("name").toString())).append("\",")
               .append(s.get("total")).append(",")
               .append(s.get("resolved")).append(",")
               .append(s.get("pending")).append(",")
               .append(String.format("%.2f", s.get("rate"))).append("\n");
        }
        return csv.toString();
    }

    public String generateFeedbackReport() {
        List<Feedback> feedbacks = feedbackRepo.findAll();
        StringBuilder csv = new StringBuilder("ID,Complaint ID,User,Rating,Comment,Date\n");
        for (Feedback f : feedbacks) {
            csv.append(f.getId()).append(",")
               .append(f.getComplaintId()).append(",")
               .append("\"").append(safe(f.getUserName())).append("\",")
               .append(f.getRating()).append(",")
               .append("\"").append(safe(f.getComment()).replace("\"", "'")).append("\",")
               .append(f.getCreatedAt() != null ? f.getCreatedAt().format(DATE_FORMAT) : "N/A").append("\n");
        }
        return csv.toString();
    }

    private String safe(String str) {
        return str == null ? "" : str;
    }
}
