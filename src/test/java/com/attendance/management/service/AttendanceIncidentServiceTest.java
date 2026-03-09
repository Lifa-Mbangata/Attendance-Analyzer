package com.attendance.management.service;

import com.attendance.management.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceIncidentServiceTest {

    private AttendanceIncidentService incidentService;
    private Employee employee;

    @BeforeEach
    void setUp() {
        incidentService = new AttendanceIncidentService();
        employee = new Employee.Builder()
                .setEmployeeId("EMP001")
                .setEmployeeName("Alice Smith")
                .build();
    }

    @Test
    void testLateArrival() {
        // Late arrival at 08:01 (Limit is 08:00)
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11)) // Monday
                .setClockIn(LocalTime.of(8, 1))
                .setClockOut(LocalTime.of(17, 30))
                .build();

        List<AttendanceIncident> incidents = incidentService.detectIncidents(record);

        assertEquals(1, incidents.size());
        assertEquals(IncidentType.LATE_ARRIVAL, incidents.get(0).getIncidentType());
        assertTrue(incidents.get(0).getDescription().contains("08:01"));
    }

    @Test
    void testEarlyDepartureMonThu() {
        // Early departure at 16:59 (Limit is 17:00)
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11)) // Monday
                .setClockIn(LocalTime.of(7, 30))
                .setClockOut(LocalTime.of(16, 59))
                .build();

        List<AttendanceIncident> incidents = incidentService.detectIncidents(record);

        assertEquals(1, incidents.size());
        assertEquals(IncidentType.EARLY_DEPARTURE, incidents.get(0).getIncidentType());
        assertTrue(incidents.get(0).getDescription().contains("16:59"));
    }

    @Test
    void testEarlyDepartureFriday() {
        // Early departure at 14:59 (Limit is 15:00)
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 1)) // Friday
                .setClockIn(LocalTime.of(7, 30))
                .setClockOut(LocalTime.of(14, 59))
                .build();

        List<AttendanceIncident> incidents = incidentService.detectIncidents(record);

        assertEquals(1, incidents.size());
        assertEquals(IncidentType.EARLY_DEPARTURE, incidents.get(0).getIncidentType());
    }

    @Test
    void testMissingClockIn() {
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11))
                .setClockOut(LocalTime.of(17, 0))
                .build();

        List<AttendanceIncident> incidents = incidentService.detectIncidents(record);

        assertEquals(1, incidents.size());
        assertEquals(IncidentType.MISSING_CLOCK_IN, incidents.get(0).getIncidentType());
    }

    @Test
    void testMissingClockOut() {
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11))
                .setClockIn(LocalTime.of(8, 0))
                .build();

        List<AttendanceIncident> incidents = incidentService.detectIncidents(record);

        assertEquals(1, incidents.size());
        assertEquals(IncidentType.MISSING_CLOCK_OUT, incidents.get(0).getIncidentType());
    }

    @Test
    void testBothMissingNoComment() {
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11))
                .build();

        List<AttendanceIncident> incidents = incidentService.detectIncidents(record);

        assertEquals(2, incidents.size());
        assertTrue(incidents.stream().anyMatch(i -> i.getIncidentType() == IncidentType.MISSING_CLOCK_IN));
        assertTrue(incidents.stream().anyMatch(i -> i.getIncidentType() == IncidentType.MISSING_CLOCK_OUT));
    }

    @Test
    void testBothMissingWithComment() {
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11))
                .setLeaveComment("Sick Leave")
                .build();

        List<AttendanceIncident> incidents = incidentService.detectIncidents(record);

        assertTrue(incidents.isEmpty());
    }

    @Test
    void testMultipleIncidents() {
        // Late arrival AND Early departure
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11))
                .setClockIn(LocalTime.of(8, 15))
                .setClockOut(LocalTime.of(16, 45))
                .build();

        List<AttendanceIncident> incidents = incidentService.detectIncidents(record);

        assertEquals(2, incidents.size());
        assertTrue(incidents.stream().anyMatch(i -> i.getIncidentType() == IncidentType.LATE_ARRIVAL));
        assertTrue(incidents.stream().anyMatch(i -> i.getIncidentType() == IncidentType.EARLY_DEPARTURE));
    }

    @Test
    void testNormalDayNoIncidents() {
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11))
                .setClockIn(LocalTime.of(8, 0))
                .setClockOut(LocalTime.of(17, 0))
                .build();

        List<AttendanceIncident> incidents = incidentService.detectIncidents(record);

        assertTrue(incidents.isEmpty());
    }
}
