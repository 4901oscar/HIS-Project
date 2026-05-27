package com.medflow.pharmacy.dto.response;

import com.medflow.pharmacy.model.MedicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private MedicationStatus status;
    private boolean lowStock;
}
