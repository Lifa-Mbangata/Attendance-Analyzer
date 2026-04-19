package com.attendance.management.service;

import com.attendance.management.domain.AttendanceRecord;
import java.util.List;

public interface ICsvParserService {
    List<AttendanceRecord> parse(String csvContent);
}
