package com.medflow.pharmacy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una prescripción médica recibida desde Clinical Service.
 * 
 * Almacena la información de la receta y los medicamentos prescritos en formato JSON.
 */
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
}
