package com.attendance.management.service;

import com.attendance.management.domain.AttendanceIncident;
import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.domain.IncidentType;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceIncidentService implements IAttendanceIncidentService {

    private static final LocalTime LATE_ARRIVAL_LIMIT = LocalTime.of(8, 0);
    private static final LocalTime EARLY_DEPARTURE_MON_THU_LIMIT = LocalTime.of(17, 0);
    private static final LocalTime EARLY_DEPARTURE_FRI_LIMIT = LocalTime.of(15, 0);

    @Override
    public List<AttendanceIncident> detectIncidents(AttendanceRecord record) {
        List<AttendanceIncident> incidents = new ArrayList<>();

        if (record.hasLeaveComment()) {
            return incidents;
        }

        // Check for missing punches
        if (!record.hasClockIn()) {
            incidents.add(new AttendanceIncident.Builder()
                    .setEmployee(record.getEmployee())
                    .setDate(record.getDate())
                    .setIncidentType(IncidentType.MISSING_CLOCK_IN)
                    .setDescription("Missing clock-in punch")
                    .build());
        }

        if (!record.hasClockOut()) {
            incidents.add(new AttendanceIncident.Builder()
                    .setEmployee(record.getEmployee())
                    .setDate(record.getDate())
                    .setIncidentType(IncidentType.MISSING_CLOCK_OUT)
                    .setDescription("Missing clock-out punch")
                    .build());
        }

        // Check for late arrival
        if (record.hasClockIn() && record.getClockIn().isAfter(LATE_ARRIVAL_LIMIT)) {
            incidents.add(new AttendanceIncident.Builder()
                    .setEmployee(record.getEmployee())
                    .setDate(record.getDate())
                    .setIncidentType(IncidentType.LATE_ARRIVAL)
                    .setDescription("Late arrival at " + record.getClockIn())
                    .build());
        }

        // Check for early departure
        if (record.hasClockOut()) {
            DayOfWeek dayOfWeek = record.getDate().getDayOfWeek();
            LocalTime departureLimit = (dayOfWeek == DayOfWeek.FRIDAY) ? EARLY_DEPARTURE_FRI_LIMIT : EARLY_DEPARTURE_MON_THU_LIMIT;

            if (record.getClockOut().isBefore(departureLimit)) {
                incidents.add(new AttendanceIncident.Builder()
                        .setEmployee(record.getEmployee())
                        .setDate(record.getDate())
                        .setIncidentType(IncidentType.EARLY_DEPARTURE)
                        .setDescription("Early departure at " + record.getClockOut())
                        .build());
            }
        }

        return incidents;
    }
}
