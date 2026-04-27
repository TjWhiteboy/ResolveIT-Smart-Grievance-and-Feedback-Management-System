package com.example.resolveit.service;

import com.example.resolveit.repository.ComplaintRepository;
import com.example.resolveit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Long> getStatusDistribution() {
        List<Object[]> results = complaintRepository.countByStatusGrouped();
        Map<String, Long> distribution = new HashMap<>();
        for (Object[] res : results) {
            String status = res[0] != null ? res[0].toString() : "Unknown";
            distribution.put(status, (Long) res[1]);
        }
        return distribution;
    }

    public Map<String, Long> getCategoryDistribution() {
        List<Object[]> results = complaintRepository.countByCategory();
        Map<String, Long> distribution = new HashMap<>();
        for (Object[] res : results) {
            String category = res[0] != null ? res[0].toString() : "Other";
            distribution.put(category, (Long) res[1]);
        }
        return distribution;
    }

    public Map<String, Long> getMonthlyTrends() {
        List<Object[]> results = complaintRepository.countByMonth();
        Map<String, Long> trends = new LinkedHashMap<>();
        // Results are ordered by month DESC, we limit to last 6 months for chart
        int count = 0;
        for (Object[] res : results) {
            if (count++ >= 6) break;
            trends.put(res[0].toString(), (Long) res[1]);
        }
        return trends;
    }

    public List<Map<String, Object>> getStaffWorkload() {
        var allComplaints = complaintRepository.findAll();
        var staffList = userRepository.findAllStaff();
        
        List<Map<String, Object>> workload = new ArrayList<>();
        for (var staff : staffList) {
            long total = allComplaints.stream()
                    .filter(c -> staff.getId().equals(c.getAssignedStaff()))
                    .count();
            long resolved = allComplaints.stream()
                    .filter(c -> staff.getId().equals(c.getAssignedStaff()) && "Resolved".equalsIgnoreCase(c.getStatus()))
                    .count();
            long pending = total - resolved;
            
            Map<String, Object> stat = new HashMap<>();
            stat.put("name", staff.getName());
            stat.put("total", total);
            stat.put("resolved", resolved);
            stat.put("pending", pending);
            stat.put("rate", total > 0 ? (double)resolved/total * 100 : 0);
            workload.add(stat);
        }
        return workload;
    }
}
