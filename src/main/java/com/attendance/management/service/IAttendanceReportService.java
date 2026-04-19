package com.attendance.management.service;

import com.attendance.management.domain.AttendanceSummary;
import com.attendance.management.dto.EmployeeIncidentReportResponse;
import com.attendance.management.dto.IncidentDetailResponse;

import java.util.List;

public interface IAttendanceReportService {
    AttendanceSummary getEmployeeSummary(String employeeId);
    List<AttendanceSummary> getDepartmentSummaries();
    AttendanceSummary getCompanySummary();
    List<IncidentDetailResponse> getEmployeeIncidents(String employeeId);
    List<EmployeeIncidentReportResponse> getCompanyIncidentReport();
}
