package com.attendance.management.service;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.domain.TimeCalculationResult;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

@Service
public class TimeCalculatorService implements ITimeCalculatorService {

    private static final LocalTime START_TIME_LIMIT = LocalTime.of(7, 30);

    private static final int MON_THU_STANDARD_MINUTES = 8 * 60 + 45;
    private static final int FRI_STANDARD_MINUTES = 7 * 60;

    private static final int MON_THU_LUNCH_MINUTES = 45;
    private static final int FRI_LUNCH_MINUTES = 30;

    @Override
    public TimeCalculationResult calculate(AttendanceRecord record) {
        DayOfWeek dayOfWeek = record.getDate().getDayOfWeek();
        int standardMinutes = (dayOfWeek == DayOfWeek.FRIDAY) ? FRI_STANDARD_MINUTES : MON_THU_STANDARD_MINUTES;
        int lunchMinutes = (dayOfWeek == DayOfWeek.FRIDAY) ? FRI_LUNCH_MINUTES : MON_THU_LUNCH_MINUTES;

        // If no clock-in and no clock-out
        if (!record.hasClockIn() && !record.hasClockOut()) {
            if (record.hasLeaveComment()) {
                return new TimeCalculationResult.Builder()
                        .setNormalMinutes(0)
                        .setWorkedMinutes(0)
                        .setShortMinutes(0)
                        .setExtraMinutes(0)
                        .setLeaveComment(record.getLeaveComment())
                        .build();
            } else {
                return new TimeCalculationResult.Builder()
                        .setNormalMinutes(0)
                        .setWorkedMinutes(0)
                        .setShortMinutes(standardMinutes)
                        .setExtraMinutes(0)
                        .build();
            }
        }

        // If one of the punches is missing (but no leave comment)
        if (!record.hasClockIn() || !record.hasClockOut()) {
            return new TimeCalculationResult.Builder()
                    .setNormalMinutes(0)
                    .setWorkedMinutes(0)
                    .setShortMinutes(standardMinutes)
                    .setExtraMinutes(0)
                    .build();
        }

        // Both punches are present
        LocalTime actualClockIn = record.getClockIn();
        LocalTime effectiveClockIn = actualClockIn.isBefore(START_TIME_LIMIT) ? START_TIME_LIMIT : actualClockIn;
        LocalTime actualClockOut = record.getClockOut();

        if (actualClockOut.isBefore(effectiveClockIn)) {
            // This case should ideally not happen but handling it for robustness
            return new TimeCalculationResult.Builder()
                    .setNormalMinutes(0)
                    .setWorkedMinutes(0)
                    .setShortMinutes(standardMinutes)
                    .setExtraMinutes(0)
                    .build();
        }

        long totalWorkedMinutes = Duration.between(effectiveClockIn, actualClockOut).toMinutes();
        long netWorkedMinutes = Math.max(0, totalWorkedMinutes - lunchMinutes);

        long shortMinutes = 0;
        long extraMinutes = 0;
        long normalMinutes = 0;

        if (netWorkedMinutes < standardMinutes) {
            shortMinutes = standardMinutes - netWorkedMinutes;
            normalMinutes = netWorkedMinutes;
        } else {
            extraMinutes = netWorkedMinutes - standardMinutes;
            normalMinutes = standardMinutes;
        }

        return new TimeCalculationResult.Builder()
                .setNormalMinutes(normalMinutes)
                .setWorkedMinutes(netWorkedMinutes)
                .setShortMinutes(shortMinutes)
                .setExtraMinutes(extraMinutes)
                .setLeaveComment(record.getLeaveComment())
                .build();
    }
}
