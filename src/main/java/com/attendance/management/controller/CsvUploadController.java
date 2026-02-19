package com.attendance.management.controller;

import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.repository.IAttendanceRecordRepository;
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
    private final IAttendanceRecordRepository repository;

    @Autowired
    public CsvUploadController(ICsvParserService csvParserService,
                               IAttendanceRecordRepository repository) {
        this.csvParserService = csvParserService;
        this.repository = repository;
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
            repository.saveAll(records);

            return ResponseEntity.ok(Map.of(
                    "message", "File uploaded successfully",
                    "recordCount", records.size()
            ));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Failed to read file: " + e.getMessage()));
        }
    }
}