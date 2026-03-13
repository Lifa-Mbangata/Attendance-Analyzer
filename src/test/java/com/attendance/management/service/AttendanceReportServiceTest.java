package com.attendance.management.service;

import com.attendance.management.domain.*;
import com.attendance.management.repository.IAttendanceIncidentRepository;
import com.attendance.management.repository.IAttendanceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AttendanceReportServiceTest {

    private IAttendanceRecordRepository recordRepository;
    private IAttendanceIncidentRepository incidentRepository;
    private ITimeCalculatorService timeCalculatorService;
    private AttendanceReportService reportService;

    @BeforeEach
    void setUp() {
        recordRepository = Mockito.mock(IAttendanceRecordRepository.class);
        incidentRepository = Mockito.mock(IAttendanceIncidentRepository.class);
        timeCalculatorService = Mockito.mock(ITimeCalculatorService.class);
        reportService = new AttendanceReportService(recordRepository, incidentRepository, timeCalculatorService);
    }

    @Test
    void testEmployeeSummaryAggregation() {
        String empId = "1001";
        Employee emp = new Employee.Builder()
                .setEmployeeId(empId)
                .setEmployeeName("John Smith")
                .setDepartment("Dept A")
                .build();

        AttendanceRecord record1 = new AttendanceRecord.Builder()
                .setEmployee(emp)
                .setDate(LocalDate.of(2025, 8, 4))
                .build();
        AttendanceRecord record2 = new AttendanceRecord.Builder()
                .setEmployee(emp)
                .setDate(LocalDate.of(2025, 8, 5))
                .build();

        when(recordRepository.findByEmployeeId(empId)).thenReturn(List.of(record1, record2));
        
        // Mock calculations
        when(timeCalculatorService.calculate(record1)).thenReturn(new TimeCalculationResult.Builder()
                .setNormalMinutes(525).setWorkedMinutes(525).setShortMinutes(0).setExtraMinutes(0).build());
        when(timeCalculatorService.calculate(record2)).thenReturn(new TimeCalculationResult.Builder()
                .setNormalMinutes(500).setWorkedMinutes(500).setShortMinutes(25).setExtraMinutes(0).build());

        // Mock incidents
        AttendanceIncident incident = new AttendanceIncident.Builder()
                .setEmployee(emp)
                .setIncidentType(IncidentType.LATE_ARRIVAL)
                .build();
        when(incidentRepository.findByEmployeeId(empId)).thenReturn(List.of(incident));

        AttendanceSummary summary = reportService.getEmployeeSummary(empId);

        assertNotNull(summary);
        assertEquals("John Smith", summary.getEntityName());
        assertEquals(1025, summary.getTotalWorkedMinutes());
        assertEquals(25, summary.getTotalShortMinutes());
        assertEquals(1, summary.getTotalIncidents());
        assertEquals(1L, summary.getIncidentTypeCounts().get(IncidentType.LATE_ARRIVAL));
    }

    @Test
    void testCompanySummaryAggregation() {
        Employee emp1 = new Employee.Builder().setEmployeeId("1").setEmployeeName("A").build();
        Employee emp2 = new Employee.Builder().setEmployeeId("2").setEmployeeName("B").build();

        AttendanceRecord r1 = new AttendanceRecord.Builder().setEmployee(emp1).setDate(LocalDate.now()).build();
        AttendanceRecord r2 = new AttendanceRecord.Builder().setEmployee(emp2).setDate(LocalDate.now()).build();

        when(recordRepository.findAll()).thenReturn(List.of(r1, r2));
        when(timeCalculatorService.calculate(any())).thenReturn(new TimeCalculationResult.Builder()
                .setWorkedMinutes(100).build());

        AttendanceIncident i1 = new AttendanceIncident.Builder().setIncidentType(IncidentType.MISSING_CLOCK_IN).build();
        when(incidentRepository.findAll()).thenReturn(List.of(i1));

        AttendanceSummary summary = reportService.getCompanySummary();

        assertNotNull(summary);
        assertEquals("Company Wide Summary", summary.getEntityName());
        assertEquals(200, summary.getTotalWorkedMinutes());
        assertEquals(1, summary.getTotalIncidents());
    }
}
