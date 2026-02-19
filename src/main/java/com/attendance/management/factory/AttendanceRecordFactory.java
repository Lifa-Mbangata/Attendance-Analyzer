package com.attendance.management.factory;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.domain.DayType;
import com.attendance.management.domain.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Component
public class AttendanceRecordFactory {

    public AttendanceRecord create(String[] row, LocalDate date) {
        Employee employee = new Employee.Builder()
                .setEmployeeId(row[0].trim())
                .setEmployeeName(row[1].trim())
                .build();

        DayType dayType = parseDayType(row[2].trim());
        LocalTime clockIn = parseTime(row[4].trim());
        LocalTime clockOut = parseTime(row[5].trim());
        String leaveComment = row[11].trim();

        return new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(date)
                .setDayType(dayType)
                .setClockIn(clockIn)
                .setClockOut(clockOut)
                .setLeaveComment(leaveComment.isEmpty() ? null : leaveComment)
                .build();
    }

    private DayType parseDayType(String value) {
        return switch (value) {
            case "Work Day" -> DayType.WORK_DAY;
            case "Rest Day" -> DayType.REST_DAY;
            default -> DayType.UNKNOWN;
        };
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}