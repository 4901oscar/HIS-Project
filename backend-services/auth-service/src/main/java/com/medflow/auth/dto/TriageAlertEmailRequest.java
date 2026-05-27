package com.medflow.auth.dto;

public class TriageAlertEmailRequest {

    private String doctorId;
    private String patientId;
    private String appointmentId;
    private String priorityLevel;
    private String priorityDescription;
    private int maxWaitMinutes;

    public TriageAlertEmailRequest() {}

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