package com.attendance.management.service;

import com.attendance.management.domain.AttendanceIncident;
import com.attendance.management.domain.AttendanceRecord;

import java.util.List;

public interface IAttendanceIncidentService {
    List<AttendanceIncident> detectIncidents(AttendanceRecord record);
}
