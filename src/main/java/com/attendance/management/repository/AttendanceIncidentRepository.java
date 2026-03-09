package com.attendance.management.repository;

import com.attendance.management.domain.AttendanceIncident;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AttendanceIncidentRepository implements IAttendanceIncidentRepository {

    private final List<AttendanceIncident> store = new ArrayList<>();

    @Override
    public void saveAll(List<AttendanceIncident> incidents) {
        store.addAll(incidents);
    }

    @Override
    public List<AttendanceIncident> findAll() {
        return new ArrayList<>(store);
    }

    @Override
    public List<AttendanceIncident> findByEmployeeId(String employeeId) {
        return store.stream()
                .filter(i -> i.getEmployee().getEmployeeId().equals(employeeId))
                .toList();
    }

    @Override
    public void clear() {
        store.clear();
    }
}
