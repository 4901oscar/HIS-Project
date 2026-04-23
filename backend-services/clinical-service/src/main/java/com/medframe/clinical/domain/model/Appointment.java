package com.medframe.clinical.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Appointment domain entity - pure Java, no Spring/JPA annotations.
 * Contains state transition business logic.
 */
public class Appointment {

    public enum AppointmentStatus {
        SCHEDULED, ACTIVE, COMPLETED, CANCELLED, MISSED
    }

    private String id;
    private String patientId;
    private String doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
    
    // Transient field - not persisted in database
    // Populated after QR generation for API response
    private String qrCodeBase64;

    public Appointment() {
        this.status = AppointmentStatus.SCHEDULED;
        this.createdAt = LocalDateTime.now();
    }

    /** Business method: SCHEDULED → ACTIVE */
    public void activate() {
        if (this.status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException(
                "Solo se pueden activar citas con estado PROGRAMADO. Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.ACTIVE;
    }

    /** Business method: ACTIVE → COMPLETED */
    public void complete() {
        if (this.status != AppointmentStatus.ACTIVE) {
            throw new IllegalStateException(
                "Solo se pueden completar citas con estado ACTIVO. Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    /** Business method: SCHEDULED | ACTIVE → CANCELLED */
    public void cancel() {
        if (this.status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("No se pueden cancelar citas ya completadas.");
        }
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("La cita ya está cancelada.");
        }
        this.status = AppointmentStatus.CANCELLED;
    }

    /** Business method: SCHEDULED → MISSED */
    public void markAsMissed() {
        if (this.status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException(
                "Solo se pueden marcar como perdidas las citas programadas. Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.MISSED;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }
}
