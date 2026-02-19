package com.attendance.management.factory;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.domain.DayType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceRecordFactoryTest {

    private AttendanceRecordFactory factory;
    private LocalDate monday;

    @BeforeEach
    void setUp() {
        factory = new AttendanceRecordFactory();
        monday = LocalDate.of(2025, 8, 4); // A Monday
    }

    // --- Test 1: Happy path - full record with clock in and out ---
    @Test
    void shouldCreateRecordWithClockInAndClockOut() {
        String[] row = {
                "1001", "John Smith", "Work Day", "1",
                "07:45", "17:10", "", "", "", "", "", "", ""
        };

        AttendanceRecord record = factory.create(row, monday);

        assertEquals("1001", record.getEmployee().getEmployeeId());
        assertEquals("John Smith", record.getEmployee().getEmployeeName());
        assertEquals(monday, record.getDate());
        assertEquals(DayType.WORK_DAY, record.getDayType());
        assertEquals(LocalTime.of(7, 45), record.getClockIn());
        assertEquals(LocalTime.of(17, 10), record.getClockOut());
        assertFalse(record.hasLeaveComment());
    }

    // --- Test 2: Missing clock in and clock out, no comment ---
    @Test
    void shouldCreateRecordWithNullTimesWhenEmpty() {
        String[] row = {
                "1002", "Jane Doe", "Work Day", "1",
                "", "", "", "", "", "", "", "", ""
        };

        AttendanceRecord record = factory.create(row, monday);

        assertFalse(record.hasClockIn());
        assertFalse(record.hasClockOut());
        assertFalse(record.hasLeaveComment());
    }

    // --- Test 3: Missing times but has a leave comment ---
    @Test
    void shouldCreateRecordWithLeaveComment() {
        String[] row = {
                "1003", "Peter Jones", "Work Day", "1",
                "", "", "", "", "", "", "", "Annual Leave", ""
        };

        AttendanceRecord record = factory.create(row, monday);

        assertFalse(record.hasClockIn());
        assertFalse(record.hasClockOut());
        assertTrue(record.hasLeaveComment());
        assertEquals("Annual Leave", record.getLeaveComment());
    }

    // --- Test 4: Work Day string maps to WORK_DAY enum ---
    @Test
    void shouldMapWorkDayStringToEnum() {
        String[] row = {
                "1004", "Amy Brown", "Work Day", "1",
                "08:00", "17:00", "", "", "", "", "", "", ""
        };

        AttendanceRecord record = factory.create(row, monday);

        assertEquals(DayType.WORK_DAY, record.getDayType());
    }

    // --- Test 5: Rest Day string maps to REST_DAY enum ---
    @Test
    void shouldMapRestDayStringToEnum() {
        String[] row = {
                "1005", "Tom White", "Rest Day", "1",
                "", "", "", "", "", "", "", "", ""
        };

        AttendanceRecord record = factory.create(row, monday);

        assertEquals(DayType.REST_DAY, record.getDayType());
    }

    // --- Test 6: Unknown day type maps to UNKNOWN enum ---
    @Test
    void shouldMapUnknownDayTypeToUnknown() {
        String[] row = {
                "1006", "Sam Green", "Holiday", "1",
                "", "", "", "", "", "", "", "", ""
        };

        AttendanceRecord record = factory.create(row, monday);

        assertEquals(DayType.UNKNOWN, record.getDayType());
    }
}