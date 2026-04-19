package com.attendance.management.repository;

import com.attendance.management.domain.AttendanceIncident;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AttendanceIncidentRepository implements IAttendanceIncidentRepository {

    // In-memory store
    private final List<AttendanceIncident> store = new ArrayList<>();

    // Save multiple incidents
    @Override
    public void saveAll(List<AttendanceIncident> incidents) {
        store.clear();
        store.addAll(incidents);
    }

    // Return all incidents
    @Override
    public List<AttendanceIncident> findAll() {
        return new ArrayList<>(store);
    }

    // Find incidents by employeeId
    @Override
    public List<AttendanceIncident> findByEmployeeId(String employeeId) {
        return store.stream()
                .filter(i -> i.getEmployee().getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    // Clear all incidents
    @Override
    public void clear() {
        store.clear();
    }
}
