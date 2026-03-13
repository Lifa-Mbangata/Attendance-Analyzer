package com.attendance.management.service;

import com.attendance.management.domain.AttendanceSummary;
import java.util.List;

public interface IAttendanceReportService {
    AttendanceSummary getEmployeeSummary(String employeeId);
    List<AttendanceSummary> getDepartmentSummaries();
    AttendanceSummary getCompanySummary();
}
