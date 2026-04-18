package com.medflow.pharmacy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para medicamentos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationResponse {
    private String id;
    private String name;
    private String description;
    private String unit;
    private Integer currentStock;
    private Integer minStock;
    private boolean active;
    private boolean lowStock; // Calculado: currentStock < minStock
}
