package com.attendance.management.factory;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.domain.DayType;
import com.attendance.management.domain.Employee;
import com.attendance.management.repository.EmployeeRepository;
import com.attendance.management.repository.IEmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class AttendanceRecordFactory {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MIN_EXPECTED_COLUMNS = 6;
    private static final int LEAVE_COMMENT_INDEX = 11;

    private final IEmployeeRepository employeeRepository;

    // Used by plain unit tests that instantiate factory directly.
    public AttendanceRecordFactory() {
        this(new EmployeeRepository());
    }

    @Autowired
    public AttendanceRecordFactory(IEmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public AttendanceRecord create(String[] columns, LocalDate date) {
        if (columns == null || columns.length < MIN_EXPECTED_COLUMNS) {
            throw new IllegalArgumentException("Invalid CSV format");
        }

        String employeeId = columns[0].trim();
        String employeeName = columns[1].trim();
        DayType dayType = parseDayType(columns[2]);
        String department = columns[3].trim();
        String leaveComment = columns.length > LEAVE_COMMENT_INDEX ? columns[LEAVE_COMMENT_INDEX].trim() : "";
        String resolvedDepartment = department.isBlank() ? "Unknown" : department;

        LocalTime clockIn = parseTime(columns[4]);
        LocalTime clockOut = parseTime(columns[5]);

        // Get or create employee, and refresh metadata if employee already exists.
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .map(existing -> {
                    existing.setEmployeeName(employeeName);
                    existing.setDepartment(resolvedDepartment);
                    return employeeRepository.save(existing);
                })
                .orElseGet(() -> {
                    Employee newEmployee = new Employee(employeeId, employeeName, resolvedDepartment);
                    return employeeRepository.save(newEmployee);
                });

        return new AttendanceRecord.Builder()
                .setEmployee(employee)
                .setDate(date)
                .setDayType(dayType)
                .setClockIn(clockIn)
                .setClockOut(clockOut)
                .setLeaveComment(leaveComment.isEmpty() ? null : leaveComment)
                .build();
    }

    private LocalTime parseTime(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : LocalTime.parse(trimmed, TIME_FORMATTER);
    }

    private DayType parseDayType(String value) {
        if (value == null) {
            return DayType.UNKNOWN;
        }

        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "work day", "workday", "work_day" -> DayType.WORK_DAY;
            case "rest day", "restday", "rest_day" -> DayType.REST_DAY;
            default -> DayType.UNKNOWN;
        };
    }
}
