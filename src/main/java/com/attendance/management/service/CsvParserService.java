
package com.attendance.management.service;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.factory.AttendanceRecordFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvParserService implements ICsvParserService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AttendanceRecordFactory factory;

    @Autowired
    public CsvParserService(AttendanceRecordFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<AttendanceRecord> parse(String csvContent) {
        List<AttendanceRecord> records = new ArrayList<>();
        LocalDate currentDate = null;

        String[] lines = csvContent.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) continue;

            if (isDateHeader(trimmed)) {
                currentDate = extractDate(trimmed);
                continue;
            }

            if (currentDate == null) continue;

            if (isWeekend(currentDate)) continue;

            String[] columns = trimmed.split(",", -1);
            if (columns.length >= 12) {
                AttendanceRecord record = factory.create(columns, currentDate);
                records.add(record);
            }
        }

        return records;
    }

    private boolean isDateHeader(String line) {
        return line.contains(" - ") && line.matches(".*\\d{2}/\\d{2}/\\d{4}.*");
    }

    private LocalDate extractDate(String headerLine) {
        String datePart = headerLine.substring(headerLine.indexOf(" - ") + 3).trim();
        return LocalDate.parse(datePart, DATE_FORMATTER);
    }

    private boolean isWeekend(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }
}