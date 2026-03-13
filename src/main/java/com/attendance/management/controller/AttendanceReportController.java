package com.attendance.management.controller;

import com.attendance.management.domain.AttendanceSummary;
import com.attendance.management.service.IAttendanceReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class AttendanceReportController {

    private final IAttendanceReportService reportService;

    @Autowired
    public AttendanceReportController(IAttendanceReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<AttendanceSummary> getEmployeeReport(@PathVariable String id) {
        AttendanceSummary summary = reportService.getEmployeeSummary(id);
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<AttendanceSummary>> getDepartmentReports() {
        return ResponseEntity.ok(reportService.getDepartmentSummaries());
    }

    @GetMapping("/company")
    public ResponseEntity<AttendanceSummary> getCompanyReport() {
        return ResponseEntity.ok(reportService.getCompanySummary());
    }
}
