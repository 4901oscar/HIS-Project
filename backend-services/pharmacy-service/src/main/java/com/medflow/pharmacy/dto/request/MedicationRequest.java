package com.medflow.pharmacy.dto.request;

import com.medflow.pharmacy.model.MedicationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationRequest {

    @NotBlank(message = "El nombre del medicamento es obligatorio")
    private String name;

    private String description;

    @NotBlank(message = "La unidad es obligatoria")
    private String unit;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Integer currentStock;

    @NotNull(message = "El stock minimo es obligatorio")
    @Min(value = 0, message = "El stock minimo no puede ser negativo")
    private Integer minStock;

    private MedicationStatus status;
}
