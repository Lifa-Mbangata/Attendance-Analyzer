package com.attendance.management;

import com.attendance.management.repository.IAttendanceIncidentRepository;
import com.attendance.management.repository.IAttendanceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AttendanceReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IAttendanceRecordRepository recordRepository;

    @Autowired
    private IAttendanceIncidentRepository incidentRepository;

    @BeforeEach
    void setUp() throws Exception {
        recordRepository.clear();
        incidentRepository.clear();

        // Data setup: Mon-Thu 8:45 standard (525 min)
        // Emp 1001: 07:30 - 17:00 (Total 570 - 45 lunch = 525) -> Standard worked
        // Emp 1002: 08:30 - 17:00 (Total 510 - 45 lunch = 465) -> 60 min short
        String csvContent =
                "Monday - 04/08/2025\n" +
                "1001,John Smith,Work Day,1,07:30,17:00,,,,,,,\n" +
                "1002,Jane Doe,Work Day,2,08:30,17:00,,,,,,,\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "attendance.csv", "text/csv", csvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/attendance/upload").file(file))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetCompanyReport() throws Exception {
        mockMvc.perform(get("/api/reports/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityName").value("Company Wide Summary"))
                .andExpect(jsonPath("$.totalWorkedMinutes").value(990)) // 525 + 465
                .andExpect(jsonPath("$.totalShortMinutes").value(60));
    }

    @Test
    void shouldGetEmployeeReport() throws Exception {
        mockMvc.perform(get("/api/reports/employee/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityName").value("John Smith"))
                .andExpect(jsonPath("$.totalWorkedMinutes").value(525));
    }

    @Test
    void shouldGetDepartmentReports() throws Exception {
        mockMvc.perform(get("/api/reports/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.entityName == 'Department: 1')]").exists())
                .andExpect(jsonPath("$[?(@.entityName == 'Department: 2')]").exists());
    }

    @Test
    void shouldGetEmployeeIncidents() throws Exception {
        mockMvc.perform(get("/api/reports/employee/1002/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].date").value("2025-08-04T00:00:00"))
                .andExpect(jsonPath("$[0].dayOfWeek").value("Monday"))
                .andExpect(jsonPath("$[0].type").value("LATE_ARRIVAL"))
                .andExpect(jsonPath("$[0].time").value("08:30"))
                .andExpect(jsonPath("$[0].details", startsWith("Late arrival at 08:30")));
    }

    @Test
    void shouldReturnEmptyIncidentsForEmployeeWithoutIncidents() throws Exception {
        mockMvc.perform(get("/api/reports/employee/1001/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetCompanyIncidentReport() throws Exception {
        mockMvc.perform(get("/api/reports/company/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employeeId").value("1001"))
                .andExpect(jsonPath("$[0].entityName").value("John Smith"))
                .andExpect(jsonPath("$[0].totalIncidents").value(0))
                .andExpect(jsonPath("$[0].incidents.length()").value(0))
                .andExpect(jsonPath("$[1].employeeId").value("1002"))
                .andExpect(jsonPath("$[1].entityName").value("Jane Doe"))
                .andExpect(jsonPath("$[1].totalIncidents").value(1))
                .andExpect(jsonPath("$[1].incidents.length()").value(1))
                .andExpect(jsonPath("$[1].incidents[0].type").value("LATE_ARRIVAL"))
                .andExpect(jsonPath("$[1].incidentTypeCounts.LATE_ARRIVAL").value(1));
    }
}
