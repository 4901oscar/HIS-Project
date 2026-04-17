package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabOrderRequest {
    
    @NotBlank(message = "Consultation ID es requerido")
    private String consultationId;
    
    @NotBlank(message = "Patient ID es requerido")
    private String patientId;
    
    @NotEmpty(message = "Debe incluir al menos un examen")
    private List<String> testNames;
}
