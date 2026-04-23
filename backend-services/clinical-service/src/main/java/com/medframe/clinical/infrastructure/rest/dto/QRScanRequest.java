package com.medframe.clinical.infrastructure.rest.dto;

/**
 * DTO for QR code scan requests.
 * Contains the decoded QR code data sent from the reception system.
 */
public class QRScanRequest {
    
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String date;
    private String time;
    private String generatedAt;
    
    public QRScanRequest() {
    }
    
    public QRScanRequest(String appointmentId, String patientId, String doctorId,
                        String date, String time, String generatedAt) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.generatedAt = generatedAt;
    }
    
    // Getters and setters
    public String getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    
    public String getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}
