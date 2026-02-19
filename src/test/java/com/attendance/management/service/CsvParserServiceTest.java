
package com.attendance.management.service;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.factory.AttendanceRecordFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserServiceTest {

    private ICsvParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new CsvParserService(new AttendanceRecordFactory());
    }

    // --- Test 1: Parses correct number of records from two sections ---
    @Test
    void shouldParseCorrectNumberOfRecords() {
        String csvContent =
                "Monday - 04/08/2025\n" +
                        "1001,John Smith,Work Day,1,07:45,17:10,,,,,,, \n" +
                        "1002,Jane Doe,Work Day,1,08:05,17:00,,,,,,, \n" +
                        "\n" +
                        "Tuesday - 05/08/2025\n" +
                        "1001,John Smith,Work Day,1,07:50,17:15,,,,,,,\n" +
                        "1002,Jane Doe,Work Day,1,,,,,,,,Annual Leave,\n";

        List<AttendanceRecord> records = parserService.parse(csvContent);

        assertEquals(4, records.size());
    }

    // --- Test 2: Correct date is assigned from the section header ---
    @Test
    void shouldAssignCorrectDateFromHeader() {
        String csvContent =
                "Monday - 04/08/2025\n" +
                        "1001,John Smith,Work Day,1,07:45,17:10,,,,,,,\n";

        List<AttendanceRecord> records = parserService.parse(csvContent);

        assertEquals(1, records.size());
        assertEquals("2025-08-04", records.get(0).getDate().toString());
    }

    // --- Test 3: Employee details are correctly parsed ---
    @Test
    void shouldParseEmployeeDetails() {
        String csvContent =
                "Monday - 04/08/2025\n" +
                        "1001,John Smith,Work Day,1,07:45,17:10,,,,,,,\n";

        List<AttendanceRecord> records = parserService.parse(csvContent);

        assertEquals("1001", records.get(0).getEmployee().getEmployeeId());
        assertEquals("John Smith", records.get(0).getEmployee().getEmployeeName());
    }


    // --- Test 4: Leave comment is parsed correctly ---
    @Test
    void shouldParseLeaveComment() {
        String csvContent =
                "Tuesday - 05/08/2025\n" +
                        "1002,Jane Doe,Work Day,1,,,,,,,,Annual Leave,\n";

        List<AttendanceRecord> records = parserService.parse(csvContent);

        assertTrue(records.get(0).hasLeaveComment());
        assertEquals("Annual Leave", records.get(0).getLeaveComment());
    }

    // --- Test 5: Weekend rows are ignored ---
    @Test
    void shouldIgnoreWeekendRows() {
        String csvContent =
                "Saturday - 09/08/2025\n" +
                        "1001,John Smith,Rest Day,1,,,,,,,,,\n" +
                        "Monday - 11/08/2025\n" +
                        "1001,John Smith,Work Day,1,07:45,17:10,,,,,,,\n";

        List<AttendanceRecord> records = parserService.parse(csvContent);

        assertEquals(1, records.size());
        assertEquals("2025-08-11", records.get(0).getDate().toString());
    }

    // --- Test 6: Empty lines are skipped ---
    @Test
    void shouldSkipEmptyLines() {
        String csvContent =
                "Monday - 04/08/2025\n" +
                        "\n" +
                        "1001,John Smith,Work Day,1,07:45,17:10,,,,,,,\n" +
                        "\n";

        List<AttendanceRecord> records = parserService.parse(csvContent);

        assertEquals(1, records.size());
    }

    // --- Test 7: Header line is not parsed as a record ---
    @Test
    void shouldNotParseHeaderLineAsRecord() {
        String csvContent =
                "Monday - 04/08/2025\n" +
                        "1001,John Smith,Work Day,1,07:45,17:10,,,,,,,\n";

        List<AttendanceRecord> records = parserService.parse(csvContent);

        assertEquals(1, records.size());
    }
}