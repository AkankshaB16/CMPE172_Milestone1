package edu.sjsu.cmpe172.careconnect.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AvailableSlotDto(
        Long slotId,
        String providerName,
        String specialization,
        String serviceName,
        Integer durationMinutes,
        BigDecimal price,
        LocalDateTime startTime,
        LocalDateTime endTime
) { }
