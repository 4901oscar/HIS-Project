package com.medframe.clinical.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Triage domain entity - pure Java, no Spring/JPA annotations.
 */
public class Triage {

    private String id;
    private String patientId;
    private String doctorId;
    private String motifId;
    private List<String> discriminatorIds;
    private PriorityLevel priorityLevel;
    private int maxWaitTimeMinutes;
    private LocalDateTime performedAt;
    private String performedBy;

    public Triage() {}

    public Triage(String patientId, String doctorId, String motifId,
                  List<String> discriminatorIds, PriorityLevel priorityLevel) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.motifId = motifId;
        this.discriminatorIds = discriminatorIds;
        this.priorityLevel = priorityLevel;
        this.maxWaitTimeMinutes = priorityLevel.getMaxWaitMinutes();
        this.performedAt = LocalDateTime.now();
        this.performedBy = doctorId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getMotifId() { return motifId; }
    public void setMotifId(String motifId) { this.motifId = motifId; }

    public List<String> getDiscriminatorIds() { return discriminatorIds; }
    public void setDiscriminatorIds(List<String> discriminatorIds) { this.discriminatorIds = discriminatorIds; }

    public PriorityLevel getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
        this.maxWaitTimeMinutes = priorityLevel.getMaxWaitMinutes();
    }

    public int getMaxWaitTimeMinutes() { return maxWaitTimeMinutes; }
    public void setMaxWaitTimeMinutes(int maxWaitTimeMinutes) { this.maxWaitTimeMinutes = maxWaitTimeMinutes; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
}
