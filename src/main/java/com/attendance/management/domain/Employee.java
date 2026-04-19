package com.attendance.management.domain;

import java.util.ArrayList;
import java.util.List;

public class Employee {

    private String employeeId;

    private String employeeName;

    private String department;

    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();

    public Employee() {}

    public Employee(String employeeId, String employeeName, String department) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
    }

    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getDepartment() { return department; }
    public List<AttendanceRecord> getAttendanceRecords() { return attendanceRecords; }

    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public void setDepartment(String department) { this.department = department; }
    
    @Override
    public String toString() {
        return "Employee{" +
                "employeeId='" + employeeId + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", department='" + department + '\'' +
                '}';
    }

    public static class Builder {
        private String employeeId;
        private String employeeName;
        private String department;

        public Builder setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
            return this;
        }

        public Builder setDepartment(String department) {
            this.department = department;
            return this;
        }

        public Employee build() {
            String resolvedDepartment = department == null || department.isBlank() ? "Unknown" : department;
            return new Employee(employeeId, employeeName, resolvedDepartment);
        }
    }
}
