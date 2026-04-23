package com.medframe.clinical.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model representing the result of a QR code scan.
 * Contains the scan status, user-friendly message, appointment details, and scan timestamp.
 */
public class ScanResult {
    
    public enum Status {
        EARLY,   // Scanned before valid window (> 15 min before appointment)
        ACTIVE,  // Scanned within valid window, appointment activated
        MISSED   // Scanned after valid window (> 60 min after appointment start)
    }
    
    private Status status;
    private String message;
    private Appointment appointment;
    private LocalDateTime scanTime;
    
    public ScanResult(Status status, String message, Appointment appointment, LocalDateTime scanTime) {
        this.status = status;
        this.message = message;
        this.appointment = appointment;
        this.scanTime = scanTime;
    }
    
    // Getters and setters
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Appointment getAppointment() {
        return appointment;
    }
    
    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }
    
    public LocalDateTime getScanTime() {
        return scanTime;
    }
    
    public void setScanTime(LocalDateTime scanTime) {
        this.scanTime = scanTime;
    }
}
