package com.medframe.clinical.domain.model;

/**
 * Manchester Triage System priority levels.
 * maxWaitMinutes = maximum wait time before the patient must be seen.
 */
public enum PriorityLevel {
    RED(0, "Inmediato"),
    ORANGE(10, "Muy urgente"),
    YELLOW(60, "Urgente"),
    GREEN(120, "Poco urgente"),
    BLUE(240, "No urgente");

    private final int maxWaitMinutes;
    private final String description;

    PriorityLevel(int maxWaitMinutes, String description) {
        this.maxWaitMinutes = maxWaitMinutes;
        this.description = description;
    }

    public int getMaxWaitMinutes() {
        return maxWaitMinutes;
    }

    public String getDescription() {
        return description;
    }
}
