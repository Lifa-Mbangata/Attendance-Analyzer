package com.attendance.management.repository;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.domain.DayType;
import com.attendance.management.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceRecordRepositoryTest {

    private IAttendanceRecordRepository repository;

    private AttendanceRecord record1;
    private AttendanceRecord record2;
    private AttendanceRecord record3;

    @BeforeEach
    void setUp() {
        repository = new AttendanceRecordRepository();

        Employee emp1 = new Employee.Builder()
                .setEmployeeId("1001")
                .setEmployeeName("John Smith")
                .build();

        Employee emp2 = new Employee.Builder()
                .setEmployeeId("1002")
                .setEmployeeName("Jane Doe")
                .build();

        record1 = new AttendanceRecord.Builder()
                .setEmployee(emp1)
                .setDate(LocalDate.of(2025, 8, 4))
                .setDayType(DayType.WORK_DAY)
                .build();

        record2 = new AttendanceRecord.Builder()
                .setEmployee(emp1)
                .setDate(LocalDate.of(2025, 8, 5))
                .setDayType(DayType.WORK_DAY)
                .build();

        record3 = new AttendanceRecord.Builder()
                .setEmployee(emp2)
                .setDate(LocalDate.of(2025, 8, 4))
                .setDayType(DayType.WORK_DAY)
                .build();
    }

    // --- Test 1: Save and retrieve all records ---
    @Test
    void shouldSaveAndReturnAllRecords() {
        repository.saveAll(List.of(record1, record2, record3));

        assertEquals(3, repository.findAll().size());
    }

    // --- Test 2: Find records by employee ID ---
    @Test
    void shouldFindRecordsByEmployeeId() {
        repository.saveAll(List.of(record1, record2, record3));

        List<AttendanceRecord> results = repository.findByEmployeeId("1001");

        assertEquals(2, results.size());
    }

    // --- Test 3: Find records by date ---
    @Test
    void shouldFindRecordsByDate() {
        repository.saveAll(List.of(record1, record2, record3));

        List<AttendanceRecord> results = repository.findByDate(LocalDate.of(2025, 8, 4));

        assertEquals(2, results.size());
    }

    // --- Test 4: Clear removes all records ---
    @Test
    void shouldClearAllRecords() {
        repository.saveAll(List.of(record1, record2, record3));
        repository.clear();

        assertEquals(0, repository.findAll().size());
    }

    // --- Test 5: SaveAll replaces existing records ---
    @Test
    void shouldReplaceRecordsOnSaveAll() {
        repository.saveAll(List.of(record1, record2));
        repository.saveAll(List.of(record3));

        assertEquals(1, repository.findAll().size());
    }
}