package com.attendance.management.domain;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class AttendanceSummary {

    private String entityName;
    private long totalNormalMinutes;
    private long totalWorkedMinutes;
    private long totalShortMinutes;
    private long totalExtraMinutes;

    private Map<IncidentType, Long> incidentTypeCounts = new TreeMap<>();

    public AttendanceSummary() {}

    private AttendanceSummary(Builder builder) {
        this.entityName = builder.entityName;
        this.totalNormalMinutes = builder.totalNormalMinutes;
        this.totalWorkedMinutes = builder.totalWorkedMinutes;
        this.totalShortMinutes = builder.totalShortMinutes;
        this.totalExtraMinutes = builder.totalExtraMinutes;
        this.incidentTypeCounts = Collections.unmodifiableMap(new TreeMap<>(builder.incidentTypeCounts));
    }

    public String getEntityName() { return entityName; }
    public long getTotalNormalMinutes() { return totalNormalMinutes; }
    public long getTotalWorkedMinutes() { return totalWorkedMinutes; }
    public long getTotalShortMinutes() { return totalShortMinutes; }
    public long getTotalExtraMinutes() { return totalExtraMinutes; }
    public Map<IncidentType, Long> getIncidentTypeCounts() { return incidentTypeCounts; }

    public long getTotalIncidents() {
        return incidentTypeCounts.values().stream().mapToLong(Long::longValue).sum();
    }

    public String getTotalNormalTimeFormatted() { return formatMinutes(totalNormalMinutes); }
    public String getTotalWorkedTimeFormatted() { return formatMinutes(totalWorkedMinutes); }
    public String getTotalShortTimeFormatted() { return formatMinutes(totalShortMinutes); }
    public String getTotalExtraTimeFormatted() { return formatMinutes(totalExtraMinutes); }

    private String formatMinutes(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    @Override
    public String toString() {
        return "AttendanceSummary{" +
                "entityName='" + entityName + '\'' +
                ", totalNormal=" + getTotalNormalTimeFormatted() +
                ", totalWorked=" + getTotalWorkedTimeFormatted() +
                ", totalShort=" + getTotalShortTimeFormatted() +
                ", totalExtra=" + getTotalExtraTimeFormatted() +
                ", totalIncidents=" + getTotalIncidents() +
                '}';
    }

    public static class Builder {
        private String entityName;
        private long totalNormalMinutes;
        private long totalWorkedMinutes;
        private long totalShortMinutes;
        private long totalExtraMinutes;
        private Map<IncidentType, Long> incidentTypeCounts = new TreeMap<>();

        public Builder setEntityName(String entityName) {
            this.entityName = entityName;
            return this;
        }

        public Builder addNormalMinutes(long minutes) {
            this.totalNormalMinutes += minutes;
            return this;
        }

        public Builder addWorkedMinutes(long minutes) {
            this.totalWorkedMinutes += minutes;
            return this;
        }

        public Builder addShortMinutes(long minutes) {
            this.totalShortMinutes += minutes;
            return this;
        }

        public Builder addExtraMinutes(long minutes) {
            this.totalExtraMinutes += minutes;
            return this;
        }

        public Builder addIncident(IncidentType type) {
            this.incidentTypeCounts.merge(type, 1L, Long::sum);
            return this;
        }

        public AttendanceSummary build() {
            return new AttendanceSummary(this);
        }
    }
}
