package com.attendance.management.dto;

import com.attendance.management.domain.IncidentType;

import java.util.List;
import java.util.Map;

public record EmployeeIncidentReportResponse(
        String employeeId,
        String entityName,
        long totalIncidents,
        Map<IncidentType, Long> incidentTypeCounts,
        List<IncidentDetailResponse> incidents
) {
}
