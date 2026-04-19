package com.attendance.management.domain;

import java.time.LocalDate;

public class AttendanceIncident {

    private Employee employee;
    private LocalDate date;
    private IncidentType incidentType;
    private String description;

    public AttendanceIncident() {}

    private AttendanceIncident(Builder builder) {
        this.employee = builder.employee;
        this.date = builder.date;
        this.incidentType = builder.incidentType;
        this.description = builder.description;
    }

    public Employee getEmployee() {
        return employee;
    }

    public LocalDate getDate() {
        return date;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "AttendanceIncident{" +
                "employee=" + employee +
                ", date=" + date +
                ", incidentType=" + incidentType +
                ", description='" + description + '\'' +
                '}';
    }

    public static class Builder {
        private Employee employee;
        private LocalDate date;
        private IncidentType incidentType;
        private String description;

        public Builder setEmployee(Employee employee) {
            this.employee = employee;
            return this;
        }

        public Builder setDate(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder setIncidentType(IncidentType incidentType) {
            this.incidentType = incidentType;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public AttendanceIncident build() {
            return new AttendanceIncident(this);
        }
    }
}
