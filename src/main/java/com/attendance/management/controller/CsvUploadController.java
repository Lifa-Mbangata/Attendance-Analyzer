package com.attendance.management.controller;

import com.attendance.management.domain.AttendanceIncident;
import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.repository.IAttendanceIncidentRepository;
import com.attendance.management.repository.IAttendanceRecordRepository;
import com.attendance.management.service.IAttendanceIncidentService;
import com.attendance.management.service.ICsvParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class CsvUploadController {

    private static final Logger logger = LoggerFactory.getLogger(CsvUploadController.class);

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
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                logger.warn("Upload attempt with empty file");
                response.put("message", "Uploaded file is empty");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("Processing file upload: {} (size: {} bytes)", file.getOriginalFilename(), file.getSize());

            // Read and parse CSV
            String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            logger.debug("CSV content read successfully. Content length: {}", csvContent.length());

            List<AttendanceRecord> records = csvParserService.parse(csvContent);
            logger.info("CSV parsed successfully. Records count: {}", records.size());

            if (records.isEmpty()) {
                logger.warn("No valid records found in CSV");
                response.put("message", "No valid attendance records found in the CSV file");
                return ResponseEntity.badRequest().body(response);
            }

            // Save records
            recordRepository.saveAll(records);
            logger.info("Saved {} attendance records to database", records.size());

            // Detect and store incidents
            List<AttendanceIncident> allIncidents = records.stream()
                    .flatMap(record -> {
                        try {
                            return incidentService.detectIncidents(record).stream();
                        } catch (Exception e) {
                            logger.error("Error detecting incidents for record: {}", record, e);
                            return java.util.stream.Stream.empty();
                        }
                    })
                    .toList();

            incidentRepository.clear();
            incidentRepository.saveAll(allIncidents);
            logger.info("Stored {} incidents", allIncidents.size());

            // Success response
            response.put("message", "File uploaded successfully");
            response.put("recordCount", records.size());
            response.put("incidentCount", allIncidents.size());
            
            logger.info("Upload completed successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("IOException during file processing", e);
            response.put("message", "Failed to read file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error during file upload", e);
            response.put("message", "Server error: " + e.getMessage());
            response.put("details", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Attendance API is running");
        return ResponseEntity.ok(response);
    }
}
