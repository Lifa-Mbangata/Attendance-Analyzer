package com.attendance.management.repository;

import com.attendance.management.domain.AttendanceRecord;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AttendanceRecordRepository implements IAttendanceRecordRepository {

    private final List<AttendanceRecord> store = new ArrayList<>();

    // Save or replace all records in memory
    @Override
    public void saveAll(List<AttendanceRecord> records) {
        store.clear();
        store.addAll(records);
    }

    // Find all records
    @Override
    public List<AttendanceRecord> findAll() {
        return new ArrayList<>(store);
    }

    // Find records by employeeId
    @Override
    public List<AttendanceRecord> findByEmployeeId(String employeeId) {
        return store.stream()
                .filter(r -> r.getEmployee().getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    // Find records by date
    @Override
    public List<AttendanceRecord> findByDate(LocalDate date) {
        return store.stream()
                .filter(r -> r.getDate().equals(date))
                .collect(Collectors.toList());
    }

    // Clear all records
    @Override
    public void clear() {
        store.clear();
    }
}
