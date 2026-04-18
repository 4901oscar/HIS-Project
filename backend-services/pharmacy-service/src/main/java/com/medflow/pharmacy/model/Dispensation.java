package com.medflow.pharmacy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa el registro de despacho de una prescripción.
 * 
 * Se crea cuando un farmacéutico despacha los medicamentos al paciente.
 */
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
}
