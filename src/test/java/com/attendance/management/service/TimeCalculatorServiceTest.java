package com.attendance.management.service;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.domain.DayType;
import com.attendance.management.domain.Employee;
import com.attendance.management.domain.TimeCalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TimeCalculatorServiceTest {

    private TimeCalculatorService calculator;
    private Employee employee;

    @BeforeEach
    void setUp() {
        calculator = new TimeCalculatorService();
        employee = new Employee.Builder()
                .setEmployeeId("123")
                .setEmployeeName("John Doe")
                .build();
    }

    @Test
    void testStandardMonThuWorkedDay() {
        // Monday: 8h 45m (525 min). Lunch: 45 min.
        // Clock in: 07:30, Clock out: 17:00. Total between: 9h 30m (570 min).
        // Worked after lunch: 570 - 45 = 525 min.
        // Short: 0, Extra: 0, Normal: 525.
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11)) // Monday
                .setDayType(DayType.WORK_DAY)
                .setClockIn(LocalTime.of(7, 30))
                .setClockOut(LocalTime.of(17, 0))
                .build();

        TimeCalculationResult result = calculator.calculate(record);
        assertEquals(525, result.getWorkedMinutes());
        assertEquals(525, result.getNormalMinutes());
        assertEquals(0, result.getShortMinutes());
        assertEquals(0, result.getExtraMinutes());
    }

    @Test
    void testStandardFridayWorkedDay() {
        // Friday: 7h (420 min). Lunch: 30 min.
        // Clock in: 07:30, Clock out: 15:00. Total between: 7h 30m (450 min).
        // Worked after lunch: 450 - 30 = 420 min.
        // Short: 0, Extra: 0, Normal: 420.
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 1)) // Friday
                .setDayType(DayType.WORK_DAY)
                .setClockIn(LocalTime.of(7, 30))
                .setClockOut(LocalTime.of(15, 0))
                .build();

        TimeCalculationResult result = calculator.calculate(record);
        assertEquals(420, result.getWorkedMinutes());
        assertEquals(420, result.getNormalMinutes());
        assertEquals(0, result.getShortMinutes());
        assertEquals(0, result.getExtraMinutes());
    }

    @Test
    void testLateArrivalMonThu() {
        // Monday. Clock in: 08:30 (instead of 07:30-08:00). Clock out: 17:00.
        // Total between: 8h 30m (510 min).
        // Worked after lunch: 510 - 45 = 465 min.
        // Short: 525 - 465 = 60 min.
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11))
                .setDayType(DayType.WORK_DAY)
                .setClockIn(LocalTime.of(8, 30))
                .setClockOut(LocalTime.of(17, 0))
                .build();

        TimeCalculationResult result = calculator.calculate(record);
        assertEquals(465, result.getWorkedMinutes());
        assertEquals(60, result.getShortMinutes());
        assertEquals(0, result.getExtraMinutes());
    }

    @Test
    void testExtraTimeFriday() {
        // Friday. Clock in: 07:30, Clock out: 16:00. Total between: 8h 30m (510 min).
        // Worked after lunch: 510 - 30 = 480 min.
        // Extra: 480 - 420 = 60 min. Normal: 420.
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 1))
                .setDayType(DayType.WORK_DAY)
                .setClockIn(LocalTime.of(7, 30))
                .setClockOut(LocalTime.of(16, 0))
                .build();

        TimeCalculationResult result = calculator.calculate(record);
        assertEquals(480, result.getWorkedMinutes());
        assertEquals(420, result.getNormalMinutes());
        assertEquals(0, result.getShortMinutes());
        assertEquals(60, result.getExtraMinutes());
    }

    @Test
    void testEarlyClockInIgnored() {
        // Monday. Clock in: 07:00. Should be capped at 07:30.
        // Clock out: 17:00. Total between: 9h 30m (570 min).
        // Worked after lunch: 570 - 45 = 525 min.
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11))
                .setClockIn(LocalTime.of(7, 0))
                .setClockOut(LocalTime.of(17, 0))
                .build();

        TimeCalculationResult result = calculator.calculate(record);
        assertEquals(525, result.getWorkedMinutes());
    }

    @Test
    void testAbsentWithComment() {
        // Monday. Absent with comment.
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11))
                .setLeaveComment("Annual Leave")
                .build();

        TimeCalculationResult result = calculator.calculate(record);
        assertEquals(0, result.getWorkedMinutes());
        assertEquals(0, result.getShortMinutes());
        assertEquals(0, result.getExtraMinutes());
        assertEquals("Annual Leave", result.getLeaveComment());
    }

    @Test
    void testAbsentWithoutComment() {
        // Monday. Absent without comment. Standard hours (525 min) lost.
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11))
                .build();

        TimeCalculationResult result = calculator.calculate(record);
        assertEquals(0, result.getWorkedMinutes());
        assertEquals(525, result.getShortMinutes());
        assertNull(result.getLeaveComment());
    }

    @Test
    void testMissingPunchWithoutComment() {
        // Monday. Only clock-in. Standard hours lost.
        AttendanceRecord record = new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(LocalDate.of(2025, 8, 11))
                .setClockIn(LocalTime.of(8, 0))
                .build();

        TimeCalculationResult result = calculator.calculate(record);
        assertEquals(0, result.getWorkedMinutes());
        assertEquals(525, result.getShortMinutes());
    }

    @Test
    void testFormatting() {
        TimeCalculationResult result = new TimeCalculationResult.Builder()
                .setWorkedMinutes(525)
                .setShortMinutes(45)
                .setExtraMinutes(5)
                .build();

        assertEquals("8:45", result.getWorkedTimeFormatted());
        assertEquals("0:45", result.getShortTimeFormatted());
        assertEquals("0:05", result.getExtraTimeFormatted());
    }
}
