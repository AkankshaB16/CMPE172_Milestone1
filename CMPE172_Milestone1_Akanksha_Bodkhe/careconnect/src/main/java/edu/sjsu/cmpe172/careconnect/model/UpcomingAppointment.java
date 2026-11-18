package edu.sjsu.cmpe172.careconnect.model;

import java.time.LocalDateTime;

public record UpcomingAppointment(
        Long appointmentId,
        Long customerId,
        String customerEmail,
        String customerName,
        String serviceName,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}