package edu.sjsu.cmpe172.careconnect.model;

import java.time.LocalDateTime;

public record AppointmentView(
        Long appointmentId,
        Long slotId,
        String customerName,
        String providerName,
        String serviceName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status
) {
}
