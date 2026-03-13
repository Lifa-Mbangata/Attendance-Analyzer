package com.attendance.management.domain;

public class Employee {

    private final String employeeId;
    private final String employeeName;
    private final String department;

    protected Employee() {
        this.employeeId = null;
        this.employeeName = null;
        this.department = null;
    }

    private Employee(Builder builder) {
        this.employeeId = builder.employeeId;
        this.employeeName = builder.employeeName;
        this.department = builder.department;
    }

    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getDepartment() { return department; }

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

        public Builder copy(Employee employee) {
            this.employeeId = employee.employeeId;
            this.employeeName = employee.employeeName;
            this.department = employee.department;
            return this;
        }

        public Employee build() {
            return new Employee(this);
        }
    }
}