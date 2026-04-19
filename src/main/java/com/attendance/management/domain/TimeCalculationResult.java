package com.attendance.management.domain;

/**
 * Result object for time calculations per attendance record
 */
public class TimeCalculationResult {
    private final long normalMinutes;
    private final long workedMinutes;
    private final long shortMinutes;
    private final long extraMinutes;
    private final String leaveComment;

    public TimeCalculationResult(Builder builder) {
        this.normalMinutes = builder.normalMinutes;
        this.workedMinutes = builder.workedMinutes;
        this.shortMinutes = builder.shortMinutes;
        this.extraMinutes = builder.extraMinutes;
        this.leaveComment = builder.leaveComment;
    }

    public long getNormalMinutes() { return normalMinutes; }
    public long getWorkedMinutes() { return workedMinutes; }
    public long getShortMinutes() { return shortMinutes; }
    public long getExtraMinutes() { return extraMinutes; }
    public String getLeaveComment() { return leaveComment; }

    public String getNormalTimeFormatted() { return formatMinutes(normalMinutes); }
    public String getWorkedTimeFormatted() { return formatMinutes(workedMinutes); }
    public String getShortTimeFormatted() { return formatMinutes(shortMinutes); }
    public String getExtraTimeFormatted() { return formatMinutes(extraMinutes); }

    private String formatMinutes(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    @Override
    public String toString() {
        return "TimeCalculationResult{" +
                "normalMinutes=" + normalMinutes +
                ", workedMinutes=" + workedMinutes +
                ", shortMinutes=" + shortMinutes +
                ", extraMinutes=" + extraMinutes +
                ", leaveComment='" + leaveComment + '\'' +
                '}';
    }

    public static class Builder {
        private long normalMinutes;
        private long workedMinutes;
        private long shortMinutes;
        private long extraMinutes;
        private String leaveComment;

        public Builder setNormalMinutes(long normalMinutes) {
            this.normalMinutes = normalMinutes;
            return this;
        }

        public Builder setWorkedMinutes(long workedMinutes) {
            this.workedMinutes = workedMinutes;
            return this;
        }

        public Builder setShortMinutes(long shortMinutes) {
            this.shortMinutes = shortMinutes;
            return this;
        }

        public Builder setExtraMinutes(long extraMinutes) {
            this.extraMinutes = extraMinutes;
            return this;
        }

        public Builder setLeaveComment(String leaveComment) {
            this.leaveComment = leaveComment;
            return this;
        }

        public TimeCalculationResult build() {
            return new TimeCalculationResult(this);
        }
    }
}
