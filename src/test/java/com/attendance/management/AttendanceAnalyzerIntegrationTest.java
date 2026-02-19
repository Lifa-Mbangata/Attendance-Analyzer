package com.attendance.management;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.repository.IAttendanceRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AttendanceAnalyzerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IAttendanceRecordRepository repository;

    // --- Test 1: Full pipeline — upload CSV, assert records stored ---
    @Test
    void shouldParseAndStoreRecordsFromUploadedCsv() throws Exception {
        String csvContent =
                "Monday - 04/08/2025\n" +
                        "1001,John Smith,Work Day,1,07:45,17:10,,,,,,,\n" +
                        "1002,Jane Doe,Work Day,1,08:05,17:00,,,,,,,\n" +
                        "\n" +
                        "Tuesday - 05/08/2025\n" +
                        "1001,John Smith,Work Day,1,07:50,17:15,,,,,,,\n" +
                        "1002,Jane Doe,Work Day,1,,,,,,,Annual Leave,\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "attendance.csv", "text/csv", csvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/attendance/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("File uploaded successfully"))
                .andExpect(jsonPath("$.recordCount").value(4));

        assertEquals(4, repository.findAll().size());
    }

    // --- Test 2: Correct employee records are stored ---
    @Test
    void shouldStoreCorrectEmployeeRecords() throws Exception {
        String csvContent =
                "Monday - 04/08/2025\n" +
                        "1001,John Smith,Work Day,1,07:45,17:10,,,,,,,\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "attendance.csv", "text/csv", csvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/attendance/upload").file(file))
                .andExpect(status().isOk());

        List<AttendanceRecord> records = repository.findByEmployeeId("1001");
        assertEquals(1, records.size());
        assertEquals("John Smith", records.get(0).getEmployee().getEmployeeName());
    }

    // --- Test 3: Weekends are excluded from stored records ---
    @Test
    void shouldExcludeWeekendsFromStoredRecords() throws Exception {
        String csvContent =
                "Saturday - 09/08/2025\n" +
                        "1001,John Smith,Rest Day,1,,,,,,,,,\n" +
                        "Monday - 11/08/2025\n" +
                        "1001,John Smith,Work Day,1,07:45,17:10,,,,,,,\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "attendance.csv", "text/csv", csvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/attendance/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordCount").value(1));

        assertEquals(1, repository.findAll().size());
    }

    // --- Test 4: Empty file returns 400 ---
    @Test
    void shouldReturn400ForEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "attendance.csv", "text/csv", new byte[0]
        );

        mockMvc.perform(multipart("/api/attendance/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Uploaded file is empty"));
    }
}