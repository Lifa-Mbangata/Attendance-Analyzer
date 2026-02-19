package com.attendance.management.domain;

public class Employee {

    private final String employeeId;
    private final String employeeName;

    protected Employee() {
        this.employeeId = null;
        this.employeeName = null;
    }

    private Employee(Builder builder) {
        this.employeeId = builder.employeeId;
        this.employeeName = builder.employeeName;
    }

    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId='" + employeeId + '\'' +
                ", employeeName='" + employeeName + '\'' +
                '}';
    }

    public static class Builder {
        private String employeeId;
        private String employeeName;

        public Builder setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
            return this;
        }

        public Builder copy(Employee employee) {
            this.employeeId = employee.employeeId;
            this.employeeName = employee.employeeName;
            return this;
        }

        public Employee build() {
            return new Employee(this);
        }
    }
}