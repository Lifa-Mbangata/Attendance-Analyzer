package com.attendance.management.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRecord {

    private Employee employee;

    private LocalDate date;

    private DayType dayType;

    private LocalTime clockIn;
    private LocalTime clockOut;
    private String leaveComment;

    public AttendanceRecord() {}

    private AttendanceRecord(Builder builder) {
        this.employee = builder.employee;
        this.date = builder.date;
        this.dayType = builder.dayType;
        this.clockIn = builder.clockIn;
        this.clockOut = builder.clockOut;
        this.leaveComment = builder.leaveComment;
    }

    public Employee getEmployee() { return employee; }
    public LocalDate getDate() { return date; }
    public DayType getDayType() { return dayType; }
    public LocalTime getClockIn() { return clockIn; }
    public LocalTime getClockOut() { return clockOut; }
    public String getLeaveComment() { return leaveComment; }

    public boolean hasClockIn() { return clockIn != null; }
    public boolean hasClockOut() { return clockOut != null; }
    public boolean hasLeaveComment() {
        return leaveComment != null && !leaveComment.isBlank();
    }

    public static class Builder {
        private Employee employee;
        private LocalDate date;
        private DayType dayType;
        private LocalTime clockIn;
        private LocalTime clockOut;
        private String leaveComment;

        public Builder setEmployee(Employee employee) {
            this.employee = employee;
            return this;
        }

        public Builder setDate(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder setDayType(DayType dayType) {
            this.dayType = dayType;
            return this;
        }

        public Builder setClockIn(LocalTime clockIn) {
            this.clockIn = clockIn;
            return this;
        }

        public Builder setClockOut(LocalTime clockOut) {
            this.clockOut = clockOut;
            return this;
        }

        public Builder setLeaveComment(String leaveComment) {
            this.leaveComment = leaveComment;
            return this;
        }

        public AttendanceRecord build() {
            return new AttendanceRecord(this);
        }
    }
}
