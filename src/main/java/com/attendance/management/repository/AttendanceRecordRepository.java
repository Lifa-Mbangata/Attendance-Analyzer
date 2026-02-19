package com.attendance.management.repository;

import com.attendance.management.domain.AttendanceRecord;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AttendanceRecordRepository implements IAttendanceRecordRepository {

    private final List<AttendanceRecord> store = new ArrayList<>();

    @Override
    public void saveAll(List<AttendanceRecord> records) {
        store.clear();
        store.addAll(records);
    }

    @Override
    public List<AttendanceRecord> findAll() {
        return new ArrayList<>(store);
    }

    @Override
    public List<AttendanceRecord> findByEmployeeId(String employeeId) {
        return store.stream()
                .filter(r -> r.getEmployee().getEmployeeId().equals(employeeId))
                .toList();
    }

    @Override
    public List<AttendanceRecord> findByDate(LocalDate date) {
        return store.stream()
                .filter(r -> r.getDate().equals(date))
                .toList();
    }

    @Override
    public void clear() {
        store.clear();
    }
}