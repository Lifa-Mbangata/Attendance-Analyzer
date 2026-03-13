package com.attendance.management.service;

import com.attendance.management.domain.*;
import com.attendance.management.repository.IAttendanceIncidentRepository;
import com.attendance.management.repository.IAttendanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AttendanceReportService implements IAttendanceReportService {

    private final IAttendanceRecordRepository recordRepository;
    private final IAttendanceIncidentRepository incidentRepository;
    private final ITimeCalculatorService timeCalculatorService;

    @Autowired
    public AttendanceReportService(IAttendanceRecordRepository recordRepository,
                                   IAttendanceIncidentRepository incidentRepository,
                                   ITimeCalculatorService timeCalculatorService) {
        this.recordRepository = recordRepository;
        this.incidentRepository = incidentRepository;
        this.timeCalculatorService = timeCalculatorService;
    }

    @Override
    public AttendanceSummary getEmployeeSummary(String employeeId) {
        List<AttendanceRecord> records = recordRepository.findByEmployeeId(employeeId);
        if (records.isEmpty()) {
            return null;
        }

        String employeeName = records.get(0).getEmployee().getEmployeeName();
        AttendanceSummary.Builder builder = new AttendanceSummary.Builder()
                .setEntityName(employeeName);

        // Aggregate time metrics
        for (AttendanceRecord record : records) {
            TimeCalculationResult result = timeCalculatorService.calculate(record);
            builder.addNormalMinutes(result.getNormalMinutes())
                   .addWorkedMinutes(result.getWorkedMinutes())
                   .addShortMinutes(result.getShortMinutes())
                   .addExtraMinutes(result.getExtraMinutes());
        }

        // Aggregate incidents
        List<AttendanceIncident> incidents = incidentRepository.findByEmployeeId(employeeId);
        for (AttendanceIncident incident : incidents) {
            builder.addIncident(incident.getIncidentType());
        }

        return builder.build();
    }

    @Override
    public List<AttendanceSummary> getDepartmentSummaries() {
        List<AttendanceRecord> allRecords = recordRepository.findAll();
        Map<String, List<AttendanceRecord>> byDept = allRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getEmployee().getDepartment()));

        return byDept.entrySet().stream()
                .map(entry -> {
                    String deptName = entry.getKey();
                    List<AttendanceRecord> deptRecords = entry.getValue();
                    AttendanceSummary.Builder builder = new AttendanceSummary.Builder()
                            .setEntityName("Department: " + deptName);

                    for (AttendanceRecord record : deptRecords) {
                        TimeCalculationResult result = timeCalculatorService.calculate(record);
                        builder.addNormalMinutes(result.getNormalMinutes())
                               .addWorkedMinutes(result.getWorkedMinutes())
                               .addShortMinutes(result.getShortMinutes())
                               .addExtraMinutes(result.getExtraMinutes());
                    }

                    // For incidents, we filter based on employee IDs in this department
                    Set<String> empIds = deptRecords.stream()
                            .map(r -> r.getEmployee().getEmployeeId())
                            .collect(Collectors.toSet());
                    
                    List<AttendanceIncident> allIncidents = incidentRepository.findAll();
                    allIncidents.stream()
                            .filter(i -> empIds.contains(i.getEmployee().getEmployeeId()))
                            .forEach(i -> builder.addIncident(i.getIncidentType()));

                    return builder.build();
                })
                .toList();
    }

    @Override
    public AttendanceSummary getCompanySummary() {
        List<AttendanceRecord> allRecords = recordRepository.findAll();
        AttendanceSummary.Builder builder = new AttendanceSummary.Builder()
                .setEntityName("Company Wide Summary");

        for (AttendanceRecord record : allRecords) {
            TimeCalculationResult result = timeCalculatorService.calculate(record);
            builder.addNormalMinutes(result.getNormalMinutes())
                   .addWorkedMinutes(result.getWorkedMinutes())
                   .addShortMinutes(result.getShortMinutes())
                   .addExtraMinutes(result.getExtraMinutes());
        }

        List<AttendanceIncident> allIncidents = incidentRepository.findAll();
        for (AttendanceIncident incident : allIncidents) {
            builder.addIncident(incident.getIncidentType());
        }

        return builder.build();
    }
}
