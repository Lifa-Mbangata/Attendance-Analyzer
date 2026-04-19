package com.attendance.management.dto;

import com.attendance.management.domain.IncidentType;

import java.time.LocalDateTime;

public record IncidentDetailResponse(
        LocalDateTime date,
        String dayOfWeek,
        IncidentType type,
        String time,
        String details
) {
}
