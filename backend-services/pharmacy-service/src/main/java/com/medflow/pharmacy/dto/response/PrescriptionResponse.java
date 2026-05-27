package com.medflow.pharmacy.dto.response;

import com.medflow.pharmacy.model.PrescriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para prescripciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {
    private String id;
    private String prescriptionCode;
    private String patientId;
    private String doctorId;
    private String medicationsJson;
    private PrescriptionStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime updatedAt;
}
