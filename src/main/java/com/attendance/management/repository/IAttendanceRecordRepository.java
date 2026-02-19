package com.attendance.management.repository;

import com.attendance.management.domain.AttendanceRecord;

import java.time.LocalDate;
import java.util.List;

public interface IAttendanceRecordRepository {
    void saveAll(List<AttendanceRecord> records);
    List<AttendanceRecord> findAll();
    List<AttendanceRecord> findByEmployeeId(String employeeId);
    List<AttendanceRecord> findByDate(LocalDate date);
    void clear();
}