package com.attendance.management.service;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.factory.AttendanceRecordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvParserService implements ICsvParserService {

    private static final Logger logger = LoggerFactory.getLogger(CsvParserService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int MIN_RECORD_COLUMNS = 6;

    private final AttendanceRecordFactory factory;

    @Autowired
    public CsvParserService(AttendanceRecordFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<AttendanceRecord> parse(String csvContent) {
        List<AttendanceRecord> records = new ArrayList<>();
        
        if (csvContent == null || csvContent.trim().isEmpty()) {
            logger.warn("Empty CSV content provided");
            return records;
        }

        LocalDate currentDate = null;
        String[] lines = csvContent.split("\n");
        int lineNumber = 0;

        for (String line : lines) {
            lineNumber++;
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                continue;
            }

            try {
                if (isDateHeader(trimmed)) {
                    try {
                        currentDate = extractDate(trimmed);
                        logger.debug("Found date header at line {}: {}", lineNumber, currentDate);
                    } catch (DateTimeParseException e) {
                        logger.warn("Could not parse date from header at line {}: {}", lineNumber, trimmed);
                        continue;
                    }
                    continue;
                }

                if (currentDate == null) {
                    logger.debug("Skipping line {} before any date header", lineNumber);
                    continue;
                }

                if (isWeekend(currentDate)) {
                    logger.debug("Skipping weekend date: {}", currentDate);
                    continue;
                }

                String[] columns = trimmed.split(",", -1);
                if (columns.length >= MIN_RECORD_COLUMNS) {
                    try {
                        AttendanceRecord record = factory.create(columns, currentDate);
                        records.add(record);
                        logger.debug("Successfully parsed record at line {}", lineNumber);
                    } catch (Exception e) {
                        logger.warn("Error parsing record at line {}: {}", lineNumber, e.getMessage());
                        // Continue with next record
                    }
                } else {
                    logger.debug("Insufficient columns at line {} (found {}, expected >= {})",
                            lineNumber, columns.length, MIN_RECORD_COLUMNS);
                }
            } catch (Exception e) {
                logger.error("Unexpected error processing line {}: {}", lineNumber, e.getMessage(), e);
            }
        }

        logger.info("CSV parsing completed. Total records: {}", records.size());
        return records;
    }

    private boolean isDateHeader(String line) {
        return line.contains(" - ") && line.matches(".*\\d{2}/\\d{2}/\\d{4}.*");
    }

    private LocalDate extractDate(String headerLine) throws DateTimeParseException {
        String datePart = headerLine.substring(headerLine.indexOf(" - ") + 3).trim();
        // Remove anything after the date (like trailing commas or spaces)
        datePart = datePart.split(",")[0].trim();
        return LocalDate.parse(datePart, DATE_FORMATTER);
    }

//    private LocalDate extractDate(String headerLine) throws DateTimeParseException {
//        String datePart = headerLine.substring(headerLine.indexOf(" - ") + 3).trim();
//        return LocalDate.parse(datePart, DATE_FORMATTER);
//    }

    private boolean isWeekend(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }
}
