package com.attendance.management.service;

import com.attendance.management.domain.AttendanceIncident;
import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.domain.IncidentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceIncidentService implements IAttendanceIncidentService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceIncidentService.class);

    private static final LocalTime LATE_ARRIVAL_LIMIT = LocalTime.of(8, 0);
    private static final LocalTime EARLY_DEPARTURE_MON_THU_LIMIT = LocalTime.of(17, 0);
    private static final LocalTime EARLY_DEPARTURE_FRI_LIMIT = LocalTime.of(15, 0);

    @Override
    public List<AttendanceIncident> detectIncidents(AttendanceRecord record) {
        List<AttendanceIncident> incidents = new ArrayList<>();

        if (record == null) {
            logger.warn("Null record provided to detectIncidents");
            return incidents;
        }

        try {
            if (record.hasLeaveComment()) {
                logger.debug("Skipping incident detection for record with leave comment: {}", record.getDate());
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
            if (record.hasClockIn() && record.getClockIn() != null) {
                if (record.getClockIn().isAfter(LATE_ARRIVAL_LIMIT)) {
                    long minutesLate = java.time.temporal.ChronoUnit.MINUTES.between(
                            LATE_ARRIVAL_LIMIT, 
                            record.getClockIn()
                    );
                    incidents.add(new AttendanceIncident.Builder()
                            .setEmployee(record.getEmployee())
                            .setDate(record.getDate())
                            .setIncidentType(IncidentType.LATE_ARRIVAL)
                            .setDescription("Late arrival at " + record.getClockIn() + " (" + minutesLate + " minutes late)")
                            .build());
                }
            }

            // Check for early departure
            if (record.hasClockOut() && record.getClockOut() != null) {
                DayOfWeek dayOfWeek = record.getDate().getDayOfWeek();
                LocalTime departureLimit = (dayOfWeek == DayOfWeek.FRIDAY) ? 
                        EARLY_DEPARTURE_FRI_LIMIT : EARLY_DEPARTURE_MON_THU_LIMIT;

                if (record.getClockOut().isBefore(departureLimit)) {
                    long minutesEarly = java.time.temporal.ChronoUnit.MINUTES.between(
                            record.getClockOut(),
                            departureLimit
                    );
                    incidents.add(new AttendanceIncident.Builder()
                            .setEmployee(record.getEmployee())
                            .setDate(record.getDate())
                            .setIncidentType(IncidentType.EARLY_DEPARTURE)
                            .setDescription("Early departure at " + record.getClockOut() + " (" + minutesEarly + " minutes early)")
                            .build());
                }
            }

            logger.debug("Detected {} incidents for date {}", incidents.size(), record.getDate());

        } catch (Exception e) {
            logger.error("Error detecting incidents for record: {}", record, e);
            // Don't throw, just log and continue
        }

        return incidents;
    }
}
