package edu.sjsu.cmpe172.careconnect.controller;

import edu.sjsu.cmpe172.careconnect.service.BookingMetrics;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class ManagementController {
    private final JdbcTemplate jdbcTemplate;
    private final BookingMetrics metrics;

    public ManagementController(JdbcTemplate jdbcTemplate, BookingMetrics metrics) {
        this.jdbcTemplate = jdbcTemplate;
        this.metrics = metrics;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("database", "UP");
        } catch (RuntimeException exception) {
            response.put("status", "DOWN");
            response.put("database", "DOWN");
        }
        return response;
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("successfulBookings", metrics.successfulBookings());
        response.put("failedBookings", metrics.failedBookings());
        response.put("averageBookingLatencyMs", metrics.averageBookingLatencyMs());
        return response;
    }
}
