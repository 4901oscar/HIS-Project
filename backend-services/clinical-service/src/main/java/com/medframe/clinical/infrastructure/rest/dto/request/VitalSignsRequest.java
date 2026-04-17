package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VitalSignsRequest {
    
    @NotBlank(message = "Patient ID es requerido")
    private String patientId;
    
    @NotNull(message = "Presión sistólica es requerida")
    @Min(value = 50, message = "Presión sistólica debe ser al menos 50 mmHg")
    @Max(value = 250, message = "Presión sistólica no puede exceder 250 mmHg")
    private Integer systolicPressure;
    
    @NotNull(message = "Presión diastólica es requerida")
    @Min(value = 30, message = "Presión diastólica debe ser al menos 30 mmHg")
    @Max(value = 150, message = "Presión diastólica no puede exceder 150 mmHg")
    private Integer diastolicPressure;
    
    @NotNull(message = "Frecuencia cardíaca es requerida")
    @Min(value = 20, message = "Frecuencia cardíaca debe ser al menos 20 bpm")
    @Max(value = 250, message = "Frecuencia cardíaca no puede exceder 250 bpm")
    private Integer heartRate;
    
    private Integer respiratoryRate;
    
    @NotNull(message = "Temperatura es requerida")
    @DecimalMin(value = "30.0", message = "Temperatura debe ser al menos 30°C")
    @DecimalMax(value = "45.0", message = "Temperatura no puede exceder 45°C")
    private Double temperature;
    
    @NotNull(message = "Saturación de oxígeno es requerida")
    @Min(value = 0, message = "Saturación debe ser al menos 0%")
    @Max(value = 100, message = "Saturación no puede exceder 100%")
    private Integer oxygenSaturation;
    
    @NotNull(message = "Peso es requerido")
    @Positive(message = "Peso debe ser positivo")
    private Double weight;
    
    @NotNull(message = "Talla es requerida")
    @Positive(message = "Talla debe ser positiva")
    private Double height;
}
