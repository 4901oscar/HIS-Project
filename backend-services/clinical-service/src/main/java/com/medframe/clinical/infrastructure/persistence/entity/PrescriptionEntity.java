package com.medframe.clinical.infrastructure.persistence.entity;

import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.infrastructure.persistence.converter.MedicationListConverter;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "prescriptions", schema = "clinical_schema",
       indexes = {
           @Index(name = "idx_prescriptions_code", columnList = "prescription_code", unique = true),
           @Index(name = "idx_prescriptions_patient", columnList = "patient_id")
       })
public class PrescriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "consultation_id", nullable = false, length = 36)
    private String consultationId;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "doctor_id", nullable = false, length = 36)
    private String doctorId;

    @Column(name = "prescription_code", nullable = false, unique = true, length = 8)
    private String prescriptionCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Convert(converter = MedicationListConverter.class)
    private List<Prescription.Medication> medications;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prescription.PrescriptionStatus status;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "issued_by", nullable = false, length = 36)
    private String issuedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", updatable = false, length = 36)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    // Constructors
    public PrescriptionEntity() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getPrescriptionCode() { return prescriptionCode; }
    public void setPrescriptionCode(String prescriptionCode) { this.prescriptionCode = prescriptionCode; }

    public List<Prescription.Medication> getMedications() { return medications; }
    public void setMedications(List<Prescription.Medication> medications) { this.medications = medications; }

    public Prescription.PrescriptionStatus getStatus() { return status; }
    public void setStatus(Prescription.PrescriptionStatus status) { this.status = status; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) issuedAt = LocalDateTime.now();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (createdBy == null) createdBy = "internal";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}