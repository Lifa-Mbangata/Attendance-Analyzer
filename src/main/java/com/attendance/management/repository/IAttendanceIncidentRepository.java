package com.attendance.management.repository;

import com.attendance.management.domain.AttendanceIncident;
import java.util.List;

public interface IAttendanceIncidentRepository {
    void saveAll(List<AttendanceIncident> incidents);
    List<AttendanceIncident> findAll();
    List<AttendanceIncident> findByEmployeeId(String employeeId);
    void clear();
}
