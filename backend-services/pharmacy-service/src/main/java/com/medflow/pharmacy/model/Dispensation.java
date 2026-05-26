package com.medflow.pharmacy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispensations", schema = "pharmacy_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dispensation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "prescription_id", nullable = false, length = 36)
    private String prescriptionId;
    
    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;
    
    @Column(name = "dispensed_at", nullable = false)
    private LocalDateTime dispensedAt;
    
    @Column(name = "dispensed_by", nullable = false, length = 36)
    private String dispensedBy;
    
    @Column(name = "dispensed_medications_json", nullable = false, columnDefinition = "TEXT")
    private String dispensedMedicationsJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (dispensedAt == null) {
            dispensedAt = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = dispensedAt;
        }
    }
}
