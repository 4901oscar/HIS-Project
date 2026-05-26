package com.medframe.clinical.infrastructure.client.dto;

public class TriageAlertRequest {

    private String doctorId;
    private String patientId;
    private String appointmentId;
    private String priorityLevel;
    private String priorityDescription;
    private int maxWaitMinutes;

    public TriageAlertRequest() {}

    public TriageAlertRequest(String doctorId, String patientId, String appointmentId,
                               String priorityLevel, String priorityDescription, int maxWaitMinutes) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.priorityLevel = priorityLevel;
        this.priorityDescription = priorityDescription;
        this.maxWaitMinutes = maxWaitMinutes;
    }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }

    public String getPriorityDescription() { return priorityDescription; }
    public void setPriorityDescription(String priorityDescription) { this.priorityDescription = priorityDescription; }

    public int getMaxWaitMinutes() { return maxWaitMinutes; }
    public void setMaxWaitMinutes(int maxWaitMinutes) { this.maxWaitMinutes = maxWaitMinutes; }
}