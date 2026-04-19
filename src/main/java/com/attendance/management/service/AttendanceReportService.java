package com.attendance.management.service;

import com.attendance.management.domain.AttendanceIncident;
import com.attendance.management.domain.AttendanceRecord;
import com.attendance.management.domain.AttendanceSummary;
import com.attendance.management.domain.IncidentType;
import com.attendance.management.domain.TimeCalculationResult;
import com.attendance.management.dto.EmployeeIncidentReportResponse;
import com.attendance.management.dto.IncidentDetailResponse;
import com.attendance.management.repository.IAttendanceRecordRepository;
import com.attendance.management.repository.IAttendanceIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AttendanceReportService implements IAttendanceReportService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale DAY_NAME_LOCALE = Locale.ENGLISH;

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

        for (AttendanceRecord record : records) {
            TimeCalculationResult result = timeCalculatorService.calculate(record);
            builder.addNormalMinutes(result.getNormalMinutes())
                   .addWorkedMinutes(result.getWorkedMinutes())
                   .addShortMinutes(result.getShortMinutes())
                   .addExtraMinutes(result.getExtraMinutes());
        }

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

    @Override
    public List<IncidentDetailResponse> getEmployeeIncidents(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            return List.of();
        }

        List<AttendanceIncident> incidents = incidentRepository.findByEmployeeId(employeeId);
        if (incidents.isEmpty()) {
            return List.of();
        }

        Map<LocalDate, AttendanceRecord> recordsByDate = recordRepository.findByEmployeeId(employeeId).stream()
                .collect(Collectors.toMap(
                        AttendanceRecord::getDate,
                        Function.identity(),
                        (first, second) -> first
                ));

        return incidents.stream()
                .sorted(Comparator.comparing(AttendanceIncident::getDate)
                        .thenComparing(incident -> incident.getIncidentType().name()))
                .map(incident -> toIncidentDetail(incident, recordsByDate.get(incident.getDate())))
                .toList();
    }

    @Override
    public List<EmployeeIncidentReportResponse> getCompanyIncidentReport() {
        List<AttendanceRecord> allRecords = recordRepository.findAll();
        List<AttendanceIncident> allIncidents = incidentRepository.findAll();

        Map<String, List<AttendanceRecord>> recordsByEmployee = allRecords.stream()
                .collect(Collectors.groupingBy(record -> record.getEmployee().getEmployeeId()));

        Map<String, List<AttendanceIncident>> incidentsByEmployee = allIncidents.stream()
                .collect(Collectors.groupingBy(incident -> incident.getEmployee().getEmployeeId()));

        Set<String> employeeIds = recordsByEmployee.keySet().stream().collect(Collectors.toSet());
        employeeIds.addAll(incidentsByEmployee.keySet());

        return employeeIds.stream()
                .sorted()
                .map(employeeId -> buildEmployeeIncidentReport(
                        employeeId,
                        recordsByEmployee.getOrDefault(employeeId, List.of()),
                        incidentsByEmployee.getOrDefault(employeeId, List.of())
                ))
                .toList();
    }

    private EmployeeIncidentReportResponse buildEmployeeIncidentReport(String employeeId,
                                                                       List<AttendanceRecord> employeeRecords,
                                                                       List<AttendanceIncident> employeeIncidents) {
        Map<LocalDate, AttendanceRecord> recordsByDate = employeeRecords.stream()
                .collect(Collectors.toMap(
                        AttendanceRecord::getDate,
                        Function.identity(),
                        (first, second) -> first
                ));

        List<IncidentDetailResponse> incidentDetails = employeeIncidents.stream()
                .sorted(Comparator.comparing(AttendanceIncident::getDate)
                        .thenComparing(incident -> incident.getIncidentType().name()))
                .map(incident -> toIncidentDetail(incident, recordsByDate.get(incident.getDate())))
                .toList();

        Map<IncidentType, Long> incidentTypeCounts = employeeIncidents.stream()
                .collect(Collectors.groupingBy(
                        AttendanceIncident::getIncidentType,
                        TreeMap::new,
                        Collectors.counting()
                ));

        return new EmployeeIncidentReportResponse(
                employeeId,
                resolveEmployeeName(employeeRecords, employeeIncidents),
                employeeIncidents.size(),
                incidentTypeCounts,
                incidentDetails
        );
    }

    private IncidentDetailResponse toIncidentDetail(AttendanceIncident incident, AttendanceRecord record) {
        return new IncidentDetailResponse(
                incident.getDate().atStartOfDay(),
                incident.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, DAY_NAME_LOCALE),
                incident.getIncidentType(),
                resolveIncidentTime(record, incident.getIncidentType()),
                incident.getDescription()
        );
    }

    private String resolveEmployeeName(List<AttendanceRecord> employeeRecords, List<AttendanceIncident> employeeIncidents) {
        if (!employeeRecords.isEmpty()) {
            return employeeRecords.get(0).getEmployee().getEmployeeName();
        }

        if (!employeeIncidents.isEmpty()) {
            return employeeIncidents.get(0).getEmployee().getEmployeeName();
        }

        return "";
    }

    private String resolveIncidentTime(AttendanceRecord record, IncidentType incidentType) {
        if (record == null) {
            return "";
        }

        return switch (incidentType) {
            case LATE_ARRIVAL -> formatTime(record.getClockIn());
            case EARLY_DEPARTURE -> formatTime(record.getClockOut());
            case MISSING_CLOCK_IN, MISSING_CLOCK_OUT -> "";
        };
    }

    private String formatTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMATTER);
    }
}
