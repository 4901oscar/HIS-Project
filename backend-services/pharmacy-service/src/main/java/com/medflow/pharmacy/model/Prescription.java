package com.medflow.pharmacy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions", schema = "pharmacy_schema", indexes = {
    @Index(name = "idx_prescriptions_patient", columnList = "patient_id"),
    @Index(name = "idx_prescriptions_status", columnList = "status"),
    @Index(name = "idx_prescriptions_code", columnList = "prescription_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "prescription_code", unique = true, nullable = false, length = 8)
    private String prescriptionCode;
    
    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;
    
    @Column(name = "doctor_id", nullable = false, length = 36)
    private String doctorId;
    
    @Column(name = "medications_json", nullable = false, columnDefinition = "TEXT")
    private String medicationsJson;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrescriptionStatus status = PrescriptionStatus.PENDING;
    
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) issuedAt = LocalDateTime.now();
        if (createdBy == null) createdBy = "internal";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
