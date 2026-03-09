package com.attendance.management.controller;

import com.attendance.management.domain.AttendanceIncident;
import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.repository.IAttendanceIncidentRepository;
import com.attendance.management.repository.IAttendanceRecordRepository;
import com.attendance.management.service.IAttendanceIncidentService;
import com.attendance.management.service.ICsvParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class CsvUploadController {

    private final ICsvParserService csvParserService;
    private final IAttendanceRecordRepository recordRepository;
    private final IAttendanceIncidentService incidentService;
    private final IAttendanceIncidentRepository incidentRepository;

    @Autowired
    public CsvUploadController(ICsvParserService csvParserService,
                               IAttendanceRecordRepository recordRepository,
                               IAttendanceIncidentService incidentService,
                               IAttendanceIncidentRepository incidentRepository) {
        this.csvParserService = csvParserService;
        this.recordRepository = recordRepository;
        this.incidentService = incidentService;
        this.incidentRepository = incidentRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Uploaded file is empty"));
        }

        try {
            String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<AttendanceRecord> records = csvParserService.parse(csvContent);
            recordRepository.saveAll(records);

            // Detect and store incidents
            List<AttendanceIncident> allIncidents = records.stream()
                    .flatMap(record -> incidentService.detectIncidents(record).stream())
                    .toList();
            incidentRepository.saveAll(allIncidents);

            return ResponseEntity.ok(Map.of(
                    "message", "File uploaded successfully",
                    "recordCount", records.size(),
                    "incidentCount", allIncidents.size()
            ));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Failed to read file: " + e.getMessage()));
        }
    }
}