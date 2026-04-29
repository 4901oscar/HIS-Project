package com.medframe.clinical.infrastructure.rest.dto;

import com.medframe.clinical.domain.model.ScanResult;
import com.medframe.clinical.infrastructure.rest.dto.response.AppointmentResponse;

import java.time.LocalDateTime;

/**
 * DTO for QR scan result responses.
 * Contains scan status, message, appointment details, and timestamp.
 */
public class ScanResultDTO {
    
    private String status;  // EARLY, ACTIVE, MISSED
    private String message;
    private AppointmentResponse appointmentDetails;
    private LocalDateTime timestamp;
    
    public ScanResultDTO() {
    }
    
    public ScanResultDTO(String status, String message, 
                        AppointmentResponse appointmentDetails, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.appointmentDetails = appointmentDetails;
        this.timestamp = timestamp;
    }
    
    /**
     * Factory method to create ScanResultDTO from domain ScanResult.
     */
    public static ScanResultDTO fromDomain(ScanResult scanResult) {
        AppointmentResponse appointmentResponse = new AppointmentResponse(
            scanResult.getAppointment().getId(),
            scanResult.getAppointment().getPatientId(),
            null,
            null,
            scanResult.getAppointment().getDoctorId(),
            scanResult.getAppointment().getAppointmentDate(),
            scanResult.getAppointment().getAppointmentTime(),
            scanResult.getAppointment().getStatus().name(),
            scanResult.getAppointment().getNotes(),
            scanResult.getAppointment().getCreatedAt(),
            null,
            scanResult.getAppointment().getInvoiceId(),
            scanResult.getAppointment().getLabInvoiceId(),
            scanResult.getAppointment().getPharmacyInvoiceId(),
            scanResult.getAppointment().isPriority()
        );
        
        return new ScanResultDTO(
            scanResult.getStatus().name(),
            scanResult.getMessage(),
            appointmentResponse,
            scanResult.getScanTime()
        );
    }
    
    // Getters and setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public AppointmentResponse getAppointmentDetails() {
        return appointmentDetails;
    }
    
    public void setAppointmentDetails(AppointmentResponse appointmentDetails) {
        this.appointmentDetails = appointmentDetails;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
