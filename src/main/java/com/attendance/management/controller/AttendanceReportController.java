package com.attendance.management.controller;

import com.attendance.management.domain.AttendanceSummary;
import com.attendance.management.dto.EmployeeIncidentReportResponse;
import com.attendance.management.dto.IncidentDetailResponse;
import com.attendance.management.service.IAttendanceReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class AttendanceReportController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceReportController.class);

    private final IAttendanceReportService reportService;

    @Autowired
    public AttendanceReportController(IAttendanceReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<AttendanceSummary> getEmployeeReport(@PathVariable String id) {
        logger.info("Fetching report for employee: {}", id);
        AttendanceSummary summary = reportService.getEmployeeSummary(id);
        if (summary == null) {
            logger.warn("No data found for employee: {}", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<AttendanceSummary>> getDepartmentReports() {
        logger.info("Fetching department reports");
        List<AttendanceSummary> summaries = reportService.getDepartmentSummaries();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/company")
    public ResponseEntity<AttendanceSummary> getCompanyReport() {
        logger.info("Fetching company report");
        AttendanceSummary summary = reportService.getCompanySummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/employee/{id}/incidents")
    public ResponseEntity<List<IncidentDetailResponse>> getEmployeeIncidents(@PathVariable String id) {
        logger.info("Fetching incidents for employee: {}", id);
        List<IncidentDetailResponse> incidents = reportService.getEmployeeIncidents(id);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/company/incidents")
    public ResponseEntity<List<EmployeeIncidentReportResponse>> getCompanyIncidentReport() {
        logger.info("Fetching company incident report");
        List<EmployeeIncidentReportResponse> report = reportService.getCompanyIncidentReport();
        return ResponseEntity.ok(report);
    }
}
