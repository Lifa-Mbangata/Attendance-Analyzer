package com.attendance.management.service;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.domain.TimeCalculationResult;

public interface ITimeCalculatorService {
    TimeCalculationResult calculate(AttendanceRecord record);
}
