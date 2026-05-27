package com.medflow.pharmacy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para dispensaciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispensationResponse {
    private String id;
    private String prescriptionId;
    private String patientId;
    private LocalDateTime dispensedAt;
    private String dispensedBy;
    private String dispensedMedicationsJson;
}
