package com.medframe.clinical.infrastructure.persistence.entity;

import com.medframe.clinical.domain.model.Appointment;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments", schema = "clinical_schema",
       indexes = {
           @Index(name = "idx_appointments_doctor_date", columnList = "doctor_id, appointment_date"),
           @Index(name = "idx_appointments_patient", columnList = "patient_id")
       })
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "doctor_id", nullable = false, length = 36)
    private String doctorId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Appointment.AppointmentStatus status;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @Column(name = "invoice_id", length = 36)
    private String invoiceId;

    @Column(name = "lab_invoice_id", length = 36)
    private String labInvoiceId;

    @Column(name = "pharmacy_invoice_id", length = 36)
    private String pharmacyInvoiceId;

    // Constructors
    public AppointmentEntity() {}

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

    public Appointment.AppointmentStatus getStatus() { return status; }
    public void setStatus(Appointment.AppointmentStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getLabInvoiceId() { return labInvoiceId; }
    public void setLabInvoiceId(String labInvoiceId) { this.labInvoiceId = labInvoiceId; }

    public String getPharmacyInvoiceId() { return pharmacyInvoiceId; }
    public void setPharmacyInvoiceId(String pharmacyInvoiceId) { this.pharmacyInvoiceId = pharmacyInvoiceId; }
}
